package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonCompressAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(JsonCompressAction.class);

    public JsonCompressAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.compress.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        Document document = editor.getDocument();
        String content = document.getText();
        String jsonStr = (JsonUtil.isJsonStr(content)) ? content : JsonUtil.extractJsonStr(content);
        String compressedJson;

        // todo 编辑器文本不属于Json，但是选中文本属于Json时，可以

        try {
            compressedJson = JsonUtil.compressJson(jsonStr);
        } catch (JsonProcessingException ex) {
            LOG.error("Json format error", ex);
            return;
        }

        String finalCompressedJson = compressedJson;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(finalCompressedJson);
        });
    }

}
