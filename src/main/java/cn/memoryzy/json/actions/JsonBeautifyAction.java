package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.Notifications;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonBeautifyAction extends DumbAwareAction {

    private static final Logger LOG = Logger.getInstance(JsonBeautifyAction.class);

    public JsonBeautifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.beautify.description"));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();

        // 选中文本
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int start = primaryCaret.getSelectionStart();
        int end = primaryCaret.getSelectionEnd();
        String selectText = document.getText(new TextRange(start, end));
        String jsonStr = (JsonUtil.isJsonStr(selectText)) ? selectText : JsonUtil.extractJsonStr(selectText);

        String formattedJson;
        boolean useSelect;
        // 如果选中了 Json 文本，就用选中的
        if (StrUtil.isNotBlank(jsonStr)) {
            useSelect = true;
        } else {
            useSelect = false;
            String documentText = document.getText();
            jsonStr = (JsonUtil.isJsonStr(documentText)) ? documentText : JsonUtil.extractJsonStr(documentText);
        }

        try {
            formattedJson = StrUtil.trim(JsonUtil.formatJson(jsonStr));
        } catch (Exception ex) {
            LOG.error("Json format error", ex);
            return;
        }

        // 可写的话就写，不可写就拷贝到剪贴板
        if (document.isWritable()) {
            // 获取当前文档内的psiFile
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String hintText;
                if (useSelect) {
                    document.replaceString(start, end, formattedJson);
                    primaryCaret.moveToOffset(start);
                    hintText = JsonAssistantBundle.messageOnSystem("hint.select.json.beautify.text");
                } else {
                    document.setText(formattedJson);
                    // 格式化
                    Optional.ofNullable(psiFile).ifPresent(el -> CodeStyleManager.getInstance(project).reformatText(psiFile, 0, document.getTextLength()));

                    primaryCaret.moveToOffset(document.getTextLength());
                    hintText = JsonAssistantBundle.messageOnSystem("hint.all.json.beautify.text");
                }

                HintManager.getInstance().showInformationHint(editor, hintText);
            });
        } else {
            PlatformUtil.setClipboard(formattedJson);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
        }

        if (useSelect) {
            primaryCaret.removeSelection();
        }
    }

}
