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
public class RemoveHistoryAction extends DumbAwareAction implements UpdateInBackground  {

    private final HistoryToolWindowComponentProvider provider;

    public RemoveHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.remove.history.text"), null, IconUtil.getRemoveIcon());
        this.provider = provider;
        registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), windowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
