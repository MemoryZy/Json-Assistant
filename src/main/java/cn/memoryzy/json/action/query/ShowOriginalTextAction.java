package cn.memoryzy.json.action.query;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.QueryState;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareToggleAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/27
 */
public class ShowOriginalTextAction extends DumbAwareToggleAction implements UpdateInBackground {

    private final QueryState queryState;
    private final JsonQueryComponentProvider queryComponentProvider;

    public ShowOriginalTextAction(QueryState queryState, JsonQueryComponentProvider queryComponentProvider) {
        super();
        this.queryState = queryState;
        this.queryComponentProvider = queryComponentProvider;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.show.original.text.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.show.original.text.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.TEXT);
    }


    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return queryState.showOriginalText;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        queryState.showOriginalText = state;
        queryComponentProvider.toggleJsonDocumentVisibility(state);
    }
}
