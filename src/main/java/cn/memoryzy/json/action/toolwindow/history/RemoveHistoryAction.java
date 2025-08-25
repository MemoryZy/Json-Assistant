package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.HistoryAffectType;
import cn.memoryzy.json.enums.HistoryDisplayMode;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.ui.tree.HistoryNode;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class RemoveHistoryAction extends DumbAwareAction implements UpdateInBackground {

    private final HistoryToolWindowComponentProvider provider;

    public RemoveHistoryAction(HistoryToolWindowComponentProvider provider, SimpleToolWindowPanel windowPanel) {
        super(JsonAssistantBundle.messageOnSystem("action.remove.history.text"), null, IconUtil.getRemoveIcon());
        this.provider = provider;
        registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), windowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<Integer> ids = new ArrayList<>();
        if (HistoryDisplayMode.LIST == provider.getHistoryState().getHistoryDisplayMode()) {
            ids.add(provider.getList().getSelectedValue().getId());

        } else {
            Optional.ofNullable(provider.getTree().getSelectionPath())
                    .map(TreePath::getLastPathComponent)
                    .map(el -> (DefaultMutableTreeNode) el)
                    .ifPresent(node -> {
                        HistoryNode historyNode = (HistoryNode) node.getUserObject();

                        if (HistoryTreeNodeType.NODE == historyNode.getNodeType()) {
                            ids.add(historyNode.getValue().getId());

                        } else {
                            // 把子节点的id都添加进去
                            List<TreeNode> nodeList = JsonAssistantUtil.enumerationToList(node.children());
                            for (TreeNode treeNode : nodeList) {
                                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeNode;
                                HistoryNode childHistoryNode = (HistoryNode) childNode.getUserObject();
                                ids.add(childHistoryNode.getValue().getId());
                            }
                        }
                    });
        }

        provider.getHistoryManager().batchRemove(ids);
        // 刷新
        provider.refreshHistoryComponent(HistoryAffectType.REMOVE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(
                provider.getTree().isEnabled()
                        && (HistoryDisplayMode.LIST == provider.getHistoryState().getHistoryDisplayMode()
                        ? null != provider.getList().getSelectedValue()
                        : provider.getTree().getSelectionCount() > 0));
    }
}
