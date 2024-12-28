package cn.memoryzy.json.action.query;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/12/27
 */
public class SwitchGroup extends DefaultActionGroup implements DumbAware {

    public SwitchGroup() {
        super(JsonAssistantBundle.messageOnSystem("action.switch.ql.text"), true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.switch.ql.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SWITCH);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        // actions.add(new JsonPathToggleAction(queryState));
        // actions.add(new JmesPathToggleAction(queryState));
        return actions.toArray(new AnAction[0]);
    }
}
