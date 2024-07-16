package cn.memoryzy.json.actions;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.Notification;
import cn.memoryzy.json.utils.PlatformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonMinifyAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(JsonMinifyAction.class);

    public JsonMinifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.minify.description"));
    }

    @Override
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

        String compressedJson;
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
            compressedJson = JsonUtil.compressJson(jsonStr);
        } catch (JsonProcessingException ex) {
            LOG.error("Json format error", ex);
            return;
        }

        if (document.isWritable()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                String hintText;
                if (useSelect) {
                    document.replaceString(start, end, compressedJson);
                    primaryCaret.moveToOffset(start);
                    hintText = JsonAssistantBundle.messageOnSystem("hint.select.json.minify.text");
                } else {
                    document.setText(compressedJson);
                    primaryCaret.moveToOffset(0);
                    hintText = JsonAssistantBundle.messageOnSystem("hint.all.json.minify.text");
                }

                HintManager.getInstance().showInformationHint(editor, hintText);
            });
        } else {
            PlatformUtil.setClipboard(compressedJson);
            Notification.notify(JsonAssistantBundle.messageOnSystem("notify.no.write.json.copy.text"), NotificationType.INFORMATION, project);
        }

        if (useSelect) {
            primaryCaret.removeSelection();
        }
    }

}
