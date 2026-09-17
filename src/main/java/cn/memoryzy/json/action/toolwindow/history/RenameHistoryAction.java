package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.action.DumbAwareBaseAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.ui.tree.HistoryNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/12/31
 */
public class RenameHistoryAction extends DumbAwareBaseAction {

    private final HistoryToolWindowComponentProvider provider;

    public RenameHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.rename.history.text"), null, IconUtil.getEditIcon());
        registerCustomShortcutSet(CustomShortcutSet.fromString("shift F6"), windowPanel);
        this.provider = provider;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        provider.showRenamePopup(getEventProject(e), provider.getSelectedRecordNodes().get(0));
    }

    @Override
    protected boolean isActionEnabled(@NotNull AnActionEvent e) {
        List<HistoryNode> selectedNodes = provider.getSelectedRecordNodes();
        // 要求单选
        if (selectedNodes.size() != 1) return false;
        // 不能选中根节点
        return super.isActionEnabled(e) && !selectedNodes.get(0).isRoot();
    }
}
