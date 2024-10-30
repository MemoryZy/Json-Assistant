package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class OpenSettingsAction extends DumbAwareAction implements CustomComponentAction {

    public OpenSettingsAction(ToolWindowEx toolWindow) {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.open.settings.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.open.settings.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SETTINGS);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt S"), toolWindow.getComponent());
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                if (Registry.is("ide.helptooltip.enabled")) {
                    HelpTooltip.dispose(this);
                    // noinspection DialogTitleCapitalization
                    new HelpTooltip()
                            .setTitle(getTemplatePresentation().getText())
                            .setShortcut(getShortcut())
                            .setDescription(JsonAssistantBundle.messageOnSystem("help.tooltip.open.settings.action.description"))
                            .installOn(this);
                } else {
                    setToolTipText(JsonAssistantBundle.messageOnSystem("help.tooltip.open.settings.action.description"));
                }
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), JsonAssistantBundle.message("setting.display.name"));
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+S";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
    }
}
