package cn.memoryzy.json.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.PluginConstant;
import cn.memoryzy.json.models.formats.BaseFormatModel;
import cn.memoryzy.json.models.formats.JsonFormatHandleModel;
import cn.memoryzy.json.models.formats.XmlFormatModel;
import cn.memoryzy.json.ui.JsonStructureDialog;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.basic.JsonViewerPanel;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.json5.Json5FileType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class JsonAssistantUtil {

    public static void showJsonStructureDialog(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        if (StrUtil.isBlank(jsonStr)) {
            return;
        }

        JsonStructureDialog dialog = new JsonStructureDialog(JSONUtil.parse(jsonStr, JsonUtil.HUTOOL_JSON_CONFIG));
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

    public static void writeOrCopyJsonOnEditor(Project project, Editor editor, Document document, String processedText,
                                               BaseFormatModel model, boolean noMinify, boolean convertFormat) {
        // 可写的话就写，不可写就拷贝到剪贴板
        if (document.isWritable() && !convertFormat) {
            // 获取当前文档内的psiFile
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String hintText;
                if (model.getSelectedText()) {
                    document.replaceString(model.getStartOffset(), model.getEndOffset(), processedText);
                    model.getPrimaryCaret().moveToOffset(model.getStartOffset());
                    hintText = model.getSelectHint();
                } else {
                    document.setText(processedText);

                    if (noMinify) {
                        // 格式化
                        Optional.ofNullable(psiFile).ifPresent(el -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));
                    }

                    model.getPrimaryCaret().moveToOffset(0);
                    hintText = model.getDefaultHint();
                }

                HintManager.getInstance().showInformationHint(editor, hintText);
            });
        } else {
            try {
                ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                ToolWindowEx toolWindow = (ToolWindowEx) getJsonViewToolWindow(project);

                if (Objects.nonNull(toolWindow)) {
                    Content content = addNewContent(project, toolWindow, contentFactory);
                    LanguageTextField languageTextField = getLanguageTextFieldOnContent(content);
                    if (Objects.nonNull(languageTextField)) {
                        languageTextField.setText(processedText);
                        toolWindow.show();
                    }
                }
            } catch (Exception e) {
                PlatformUtil.setClipboard(processedText);
                Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
            } finally {
                if (model.getSelectedText()) {
                    model.getPrimaryCaret().removeSelection();
                }
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static BaseFormatModel matchFormats(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }

        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();
        String selectText = document.getText(new TextRange(startOffset, endOffset));
        String documentText = document.getText();

        BaseFormatModel model = new XmlFormatModel(startOffset, endOffset, primaryCaret);
        BaseFormatModel.fillModel(project, document, selectText, documentText, model);

        if (StrUtil.isBlank(model.getContent())) {
            return null;
        }

        // 其他格式 .........

        return model;
    }


    public static String truncateText(String text, int maxLength, String omitHint) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + " " + omitHint;
        } else {
            return text;
        }
    }

    public static Class<?> getClass(String classQualifiedName) {
        try {
            return Class.forName(classQualifiedName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Object getStaticFinalFieldValue(Class<?> clz, String fieldName) {
        Field matchField = null;
        try {
            for (Field field : ClassUtil.getDeclaredFields(clz)) {
                if (Objects.equals(fieldName, field.getName())) {
                    // 检查字段是否是静态且Final的
                    if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        matchField = field;
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return Objects.isNull(matchField) ? null : ReflectUtil.getStaticFieldValue(matchField);
    }

    /**
     * 是否不允许在当前 JSON 文档内写入；true，不写；false：写入
     */
    public static boolean isNotWriteJsonDoc(AnActionEvent e, Project project, Document document, JsonFormatHandleModel model) {
        return isNotWriteDoc(e, project, document, model, JsonFileType.INSTANCE, Json5FileType.INSTANCE);
    }

    /**
     * 是否不允许在当前 XML 文档内写入；true，不写；false：写入
     */
    public static boolean isNotWriteXmlDoc(AnActionEvent e, Project project, Document document, JsonFormatHandleModel model) {
        return isNotWriteDoc(e, project, document, model, XmlFileType.INSTANCE);
    }

    /**
     * 是否不允许在当前文档内写入；true，不写；false：写入
     */
    public static boolean isNotWriteDoc(AnActionEvent e, Project project, Document document, JsonFormatHandleModel model, FileType... fileTypes) {
        // 是否在当前文档内写入；true，不写；false：写入
        boolean isNotWriteDoc;
        // 文档若可写入，且不在控制台内
        if (document.isWritable() && !isOnConsole(e)) {
            // 当前有无选中文本
            if (model.getSelectedText()) {
                // 选中了文本，将在选中区域内写入更改后的文本
                isNotWriteDoc = false;
            } else {
                FileType fileType = PlatformUtil.getDocumentFileType(project, document);
                // 若未选中文本，判断是否符合文件类型，符合的话也可写入（置为false，表示可写入）
                isNotWriteDoc = !Arrays.asList(fileTypes).contains(fileType);
            }
        } else {
            // 不可写入
            isNotWriteDoc = true;
        }

        return isNotWriteDoc;
    }

    /**
     * true，处于控制台；false反之。
     */
    public static boolean isOnConsole(AnActionEvent e) {
        ConsoleView data = e.getData(LangDataKeys.CONSOLE_VIEW);
        return data != null;
    }

    public static ToolWindow getJsonViewToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID);
    }

    public static Content getSelectedContent(ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        Content selectedContent = contentManager.getSelectedContent();
        if (Objects.isNull(selectedContent)) {
            selectedContent = contentManager.getContent(0);
        }

        return selectedContent;
    }

    public static LanguageTextField getLanguageTextFieldOnContent(Content content) {
        if (Objects.nonNull(content)) {
            SimpleToolWindowPanel windowPanel = (SimpleToolWindowPanel) content.getComponent();
            JsonViewerPanel viewerPanel = (JsonViewerPanel) windowPanel.getContent();
            if (Objects.nonNull(viewerPanel)) {
                return viewerPanel.getJsonTextField();
            }
        }

        return null;
    }

    public static Content addNewContent(Project project, ToolWindowEx toolWindow, ContentFactory contentFactory) {
        ContentManager contentManager = toolWindow.getContentManager();
        int contentCount = contentManager.getContentCount();
        String displayName = PluginConstant.JSON_VIEWER_TOOL_WINDOW_DISPLAY_NAME + " " + (contentCount + 1);

        JsonViewerWindow window = new JsonViewerWindow(project, false);
        Content content = contentFactory.createContent(window.getRootPanel(), displayName, false);
        contentManager.addContent(content, contentCount);
        contentManager.setSelectedContent(content, true);
        return content;
    }
}
