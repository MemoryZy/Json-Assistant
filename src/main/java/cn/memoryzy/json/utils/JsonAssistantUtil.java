package cn.memoryzy.json.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.model.JsonEditorInfoModel;
import cn.memoryzy.json.model.formats.BaseFormatModel;
import cn.memoryzy.json.ui.JsonStructureDialog;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;

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

    public static void writeOrCopyJsonOnEditor(Project project, Editor editor,
                                               Document document, String processedText,
                                               JsonEditorInfoModel info, String selectHint, String defaultHint) {
        // 可写的话就写，不可写就拷贝到剪贴板
        if (document.isWritable()) {
            // 获取当前文档内的psiFile
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String hintText;
                if (info.isSelectedText) {
                    document.replaceString(info.startOffset, info.endOffset, processedText);
                    info.primaryCaret.moveToOffset(info.startOffset);
                    hintText = selectHint;
                } else {
                    document.setText(processedText);
                    // 格式化
                    Optional.ofNullable(psiFile).ifPresent(el -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));

                    info.primaryCaret.moveToOffset(document.getTextLength());
                    hintText = defaultHint;
                }

                HintManager.getInstance().showInformationHint(editor, hintText);
            });
        } else {
            PlatformUtil.setClipboard(processedText);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
        }

        if (info.isSelectedText) {
            info.primaryCaret.removeSelection();
        }
    }

    public static BaseFormatModel obtainFormatModel(Editor editor) {
        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();
        String selectText = document.getText(new TextRange(startOffset, endOffset));
        String jsonContent = (JsonUtil.isJsonStr(selectText)) ? selectText : JsonUtil.extractJsonStr(selectText);

        boolean isSelectedText = true;
        if (StrUtil.isBlank(jsonContent)) {
            isSelectedText = false;
            String documentText = document.getText();
            jsonContent = (JsonUtil.isJsonStr(documentText)) ? documentText : JsonUtil.extractJsonStr(documentText);
        }


        return null;
    }

}
