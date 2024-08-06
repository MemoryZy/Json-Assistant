package cn.memoryzy.json.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.formats.BaseFormatModel;
import cn.memoryzy.json.model.formats.XmlFormatModel;
import cn.memoryzy.json.ui.JsonStructureDialog;
import cn.memoryzy.json.ui.basic.JsonViewPanel;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.LanguageTextField;

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

        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(false);
        JsonStructureDialog dialog = new JsonStructureDialog(JSONUtil.parse(jsonStr, jsonConfig));
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
                    int moveToOffset = 0;

                    if (noMinify) {
                        // 格式化
                        Optional.ofNullable(psiFile).ifPresent(el -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));
                        moveToOffset = document.getTextLength();
                    }

                    model.getPrimaryCaret().moveToOffset(moveToOffset);
                    hintText = model.getDefaultHint();
                }

                HintManager.getInstance().showInformationHint(editor, hintText);
            });
        } else {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID);
            JsonViewPanel panel = (JsonViewPanel) PlatformUtil.getMainComponentWithOpenToolWindow(toolWindow);
            if (toolWindow != null && panel != null) {
                LanguageTextField jsonTextField = panel.getJsonTextField();
                jsonTextField.setText(processedText);
                toolWindow.show();
            } else {
                PlatformUtil.setClipboard(processedText);
                Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
            }
        }

        if (model.getSelectedText()) {
            model.getPrimaryCaret().removeSelection();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static BaseFormatModel matchFormats(Editor editor) {
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
        BaseFormatModel.fillModel(selectText, documentText, model);

        if (StrUtil.isBlank(model.getContent())) {
            return null;
        }

        // 其他格式 .........

        return model;
    }

}
