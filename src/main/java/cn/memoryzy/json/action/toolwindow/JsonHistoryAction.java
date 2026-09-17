package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.HistoryState;
import cn.memoryzy.json.toolwindow.HistoryToolWindowManager;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    private final HistoryState historyState;

    public JsonHistoryAction(ToolWindowEx toolWindow) {
        super();
        this.historyState = ToolWindowSettings.getInstance().getHistoryState();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.history.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.HISTORY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt H"), toolWindow.getComponent());
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                // noinspection DialogTitleCapitalization
                new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setShortcut(getShortcut())
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.history.description"))
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (null == project) return;
        HistoryToolWindowManager manager = HistoryToolWindowManager.getInstance(project);
        // 展示窗口
        manager.show();
        // 请求焦点给历史记录组件
        manager.getProvider().requestFocusOnStructureComponent();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(null != getEventProject(e) && historyState.isEnableHistory());
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+H";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
    }

}

