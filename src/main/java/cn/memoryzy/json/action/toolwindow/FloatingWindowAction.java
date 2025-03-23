package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FloatingWindowAction extends ToggleAction implements DumbAware, UpdateInBackground {

    private final ToolWindow toolWindow;

    public FloatingWindowAction(ToolWindow toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.floating.window.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.floating.window.description"));
        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl alt F"), toolWindow.getComponent());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return Objects.equals(ToolWindowType.FLOATING, toolWindow.getType());
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean state) {
        if (state) {
            toolWindow.setType(ToolWindowType.FLOATING, null);
        } else {
            toolWindow.setType(ToolWindowType.DOCKED, null);
        }
    }
}