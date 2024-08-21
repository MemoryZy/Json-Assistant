package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FloatingWindowAction extends ToggleAction implements DumbAware {

    private final ToolWindowEx toolWindow;

    public FloatingWindowAction(ToolWindowEx toolWindow) {
        super(JsonAssistantBundle.message("action.floating.window.text"),
                JsonAssistantBundle.messageOnSystem("action.floating.window.description"),
                null);

        this.toolWindow = toolWindow;
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt F"), toolWindow.getComponent());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return Objects.equals(ToolWindowType.FLOATING, toolWindow.getType());
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (state) {
            toolWindow.setType(ToolWindowType.FLOATING, null);
        } else {
            toolWindow.setType(ToolWindowType.DOCKED, null);
        }
    }
}