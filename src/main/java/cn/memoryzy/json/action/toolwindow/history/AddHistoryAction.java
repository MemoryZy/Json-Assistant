package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class AddHistoryAction extends DumbAwareAction implements UpdateInBackground {

    private final HistoryToolWindowComponentProvider provider;

    public AddHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.add.history.text"), null, IconUtil.getAddIcon());
        this.provider = provider;
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt INSERT"), windowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        provider.executeAddAction();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(provider.addActionUpdate());
    }
}
