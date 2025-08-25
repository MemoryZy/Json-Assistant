package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class EditHistoryAction extends DumbAwareAction implements UpdateInBackground {

    private final HistoryToolWindowComponentProvider provider;

    public EditHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.edit.history.text"), null, IconUtil.getEditIcon());
        this.provider = provider;
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt E"), windowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        provider.executeEditAction();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 确保此时不处于编辑模式，并且选中了元素
        JsonRecord record = provider.getCurrentSelectionValue();
        e.getPresentation().setEnabled(provider.getTree().isEnabled() && Objects.nonNull(record));
    }
}
