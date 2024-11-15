package cn.memoryzy.json.action;

import cn.memoryzy.json.action.group.JsonAssistantPopupGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonAssistantAction extends DumbAwareAction implements UpdateInBackground {

    public JsonAssistantAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.assistant.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.assistant.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        new JsonAssistantPopupGroup(true).showPopupMenu(event.getDataContext());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Presentation presentation = event.getPresentation();
        presentation.setVisible(false);
        presentation.setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), PlatformUtil.getEditor(dataContext)));
    }

}
