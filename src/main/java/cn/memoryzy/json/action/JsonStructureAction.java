package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.JsonFormatHandleModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAction extends DumbAwareAction {

    public JsonStructureAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.structure.description"));
        presentation.setIcon(JsonAssistantIcons.STRUCTURE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = getEventProject(event);
        Editor editor = PlatformUtil.getEditor(event);
        JsonFormatHandleModel model = JsonFormatHandleModel.of(project, editor);
        JsonAssistantUtil.showJsonStructureDialog(model.getContent());
    }

}
