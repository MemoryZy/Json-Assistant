package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.formats.BaseFormatModel;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class OtherFormatsToJsonAction extends DumbAwareAction implements UpdateInBackground {

    public OtherFormatsToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.other.formats.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.other.formats.to.json.description"));
        presentation.setIcon(PlatformUtil.isNewUi() ? JsonAssistantIcons.ExpUi.NEW_ROTATE : JsonAssistantIcons.ROTATE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        try {
            BaseFormatModel model = JsonAssistantUtil.matchFormats(e.getProject(), editor);
            if (Objects.nonNull(model)) {
                JsonAssistantUtil.writeOrCopyJsonOnEditor(e.getProject(), editor, editor.getDocument(),
                        model.convertToJson(), model, true, true);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        if (Objects.nonNull(project) && Objects.nonNull(editor)) {
            try {
                BaseFormatModel model = JsonAssistantUtil.matchFormats(project, editor);
                if (Objects.nonNull(model)) {
                    enabled = true;
                    presentation.setText(model.getActionName());
                    presentation.setDescription(model.getActionDescription());
                }
            } catch (Exception ignored) {
            }
        }
        presentation.setEnabledAndVisible(enabled);
    }

}
