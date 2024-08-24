package cn.memoryzy.json.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.constants.PluginConstant;
import cn.memoryzy.json.models.formats.BaseFormatModel;
import cn.memoryzy.json.models.formats.JsonFormatHandleModel;
import cn.memoryzy.json.models.formats.XmlFormatModel;
import cn.memoryzy.json.ui.JsonStructureDialog;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.basic.JsonViewerPanel;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.json5.Json5FileType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.StreamUtil;
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
import com.intellij.util.Urls;
import com.intellij.util.ui.UIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class JsonAssistantUtil {

    private static final Logger LOG = Logger.getInstance(JsonAssistantUtil.class);

    public static void showJsonStructureDialog(String text) {
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        if (StrUtil.isBlank(jsonStr)) {
            return;
        }

        JsonStructureDialog dialog = new JsonStructureDialog(JSONUtil.parse(jsonStr, JsonUtil.HUTOOL_JSON_CONFIG));
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

    public static void applyProcessedTextToDocumentOrClipboard(Project project, Editor editor, Document document,
                                                               String processedText, BaseFormatModel model,
                                                               boolean noMinify, boolean convertFormat) {
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
    public static BaseFormatModel createFormatModelFromEditor(Project project, Editor editor) {
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
        BaseFormatModel.prepareModel(project, document, selectText, documentText, model);

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

    public static Class<?> getClassByName(String classQualifiedName) {
        try {
            return Class.forName(classQualifiedName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Object readStaticFinalFieldValue(Class<?> clz, String fieldName) {
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
        return isWriteDocForbidden(e, project, document, model, JsonFileType.INSTANCE, Json5FileType.INSTANCE);
    }

    /**
     * 是否不允许在当前 XML 文档内写入；true，不写；false：写入
     */
    public static boolean isNotWriteXmlDoc(AnActionEvent e, Project project, Document document, JsonFormatHandleModel model) {
        return isWriteDocForbidden(e, project, document, model, XmlFileType.INSTANCE);
    }

    /**
     * 是否不允许在当前文档内写入；true，不允许写入；false：允许写入
     */
    public static boolean isWriteDocForbidden(AnActionEvent e, Project project, Document document, JsonFormatHandleModel model, FileType... fileTypes) {
        // 文档不可写入，或在控制台内，返回不允许写
        if (!document.isWritable() || inConsole(e)) {
            return true;
        }

        // 选中了文本，将在选中区域内写入更改后的文本，返回允许写入
        if (model.getSelectedText()) {
            return false;
        }

        FileType fileType = PlatformUtil.getDocumentFileType(project, document);
        // 若未选中文本，判断是否符合文件类型，符合的话也可写入（置为false，表示可写入）
        return !Arrays.asList(fileTypes).contains(fileType);
    }


    /**
     * 是否处于控制台；true，处于控制台；false反之。
     */
    public static boolean inConsole(AnActionEvent e) {
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


    public static void openOnlineDoc(Project project, boolean useHtmlEditor) {
        String url = HyperLinks.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();

        if (PlatformUtil.canBrowseInHTMLEditor() && useHtmlEditor) {
            String timeoutContent = null;
            try (InputStream html = JsonAssistantUtil.class.getResourceAsStream("timeout.html")) {
                if (html != null) {
                    timeoutContent = new String(StreamUtil.readBytes(html), StandardCharsets.UTF_8)
                            .replace("__THEME__", darkTheme ? "theme-dark" : "")
                            .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.title"))
                            .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.message"))
                            .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.action", url));
                }
            } catch (IOException ex) {
                LOG.error(ex);
            }

            if (HyperLinks.isReachable()) {
                HTMLEditorProvider.openEditor(project, JsonAssistantBundle.messageOnSystem("html.editor.quick.start.title"), url, timeoutContent);
                return;
            }
        }

        BrowserUtil.browse(url);
    }


    public static boolean isJsonOrExtract(String text) {
        text = StrUtil.trim(text);
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        return StrUtil.isNotBlank(jsonStr);
    }
}
