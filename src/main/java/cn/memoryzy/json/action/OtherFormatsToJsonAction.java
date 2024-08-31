package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.BaseFormatModel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        try {
            BaseFormatModel model = JsonAssistantUtil.createFormatModelFromEditor(e.getProject(), editor);
            if (Objects.nonNull(model)) {
                JsonAssistantUtil.applyProcessedTextToDocumentOrClipboard(e.getProject(), editor, editor.getDocument(),
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
                BaseFormatModel model = JsonAssistantUtil.createFormatModelFromEditor(project, editor);
                if (Objects.nonNull(model)) {
                    enabled = true;
                    updateActionIfNeeded(presentation, model);
                }
            } catch (Exception ignored) {
            }
        }

        presentation.setEnabledAndVisible(enabled);
    }


    private void updateActionIfNeeded(Presentation presentation, BaseFormatModel model) {
        String actionName = model.getActionName();
        String actionDescription = model.getActionDescription();
        Icon actionIcon = model.getActionIcon();

        String text = presentation.getText();
        String description = presentation.getDescription();
        Icon icon = presentation.getIcon();

        if (!Objects.equals(actionName, text)) presentation.setText(model.getActionName());
        if (!Objects.equals(actionDescription, description)) presentation.setDescription(model.getActionDescription());
        if (!Objects.equals(actionIcon, icon)) presentation.setIcon(model.getActionIcon());
    }

}
