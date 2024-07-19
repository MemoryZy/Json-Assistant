package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FloatingWindowAction extends AnAction {

    private final ToolWindowEx toolWindow;

    public FloatingWindowAction(ToolWindowEx toolWindow) {
        super(JsonAssistantBundle.messageOnSystem("action.floating.window.text"),
                JsonAssistantBundle.messageOnSystem("action.floating.window.description"),
                JsonAssistantIcons.MINIFY);

        this.toolWindow = toolWindow;
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt F"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindowType toolWindowType = toolWindow.getType();
        if (Objects.equals(ToolWindowType.FLOATING, toolWindowType)){
            toolWindow.setType(ToolWindowType.DOCKED, null);
        } else {
            toolWindow.setType(ToolWindowType.FLOATING, null);
        }
    }
}