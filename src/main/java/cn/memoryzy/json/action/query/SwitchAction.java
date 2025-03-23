package cn.memoryzy.json.action.query;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.QueryState;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.tools.SimpleActionGroup;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/27
 */
public class SwitchAction extends DumbAwareAction implements UpdateInBackground {

    private final QueryState queryState;
    private final JsonQueryComponentProvider queryComponentProvider;

    public SwitchAction(QueryState queryState, JsonQueryComponentProvider queryComponentProvider) {
        super();
        this.queryState = queryState;
        this.queryComponentProvider = queryComponentProvider;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.switch.ql.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.switch.ql.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SWITCH);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new JsonPathToggleAction(queryState, queryComponentProvider));
        actionGroup.add(new JmesPathToggleAction(queryState, queryComponentProvider));

        JBPopupFactory.getInstance()
                .createActionGroupPopup(null, actionGroup, e.getDataContext(), JBPopupFactory.ActionSelectionAid.MNEMONICS, true)
                .showUnderneathOf((Component) e.getInputEvent().getSource());
    }


}
