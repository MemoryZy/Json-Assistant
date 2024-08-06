package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonMinifyAction extends DumbAwareAction {
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
        JsonFormatHandleModel model = JsonFormatHandleModel.of(editor,
                JsonAssistantBundle.messageOnSystem("hint.select.json.minify.text"),
                JsonAssistantBundle.messageOnSystem("hint.all.json.minify.text"));

        String compressedJson;
        try {
            compressedJson = JsonUtil.compressJson(model.getContent());
        } catch (JsonProcessingException ex) {
            LOG.error("Json format error", ex);
            return;
        }

        JsonAssistantUtil.writeOrCopyJsonOnEditor(project, editor, document, compressedJson, model, false, false);
    }

}
