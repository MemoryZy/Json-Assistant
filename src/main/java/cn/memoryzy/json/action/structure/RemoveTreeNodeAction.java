package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Map;

public class RemoveTreeNodeAction extends DumbAwareAction {

    private final Tree tree;

    public RemoveTreeNodeAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.remove.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.remove.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            // 记录树节点展开状态
            Map<TreePath, Boolean> expandedStates = UIManager.recordExpandedStates(tree);

            for (TreePath path : paths) {
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                JsonTreeNode parent = (JsonTreeNode) node.getParent();
                if (parent != null) {
                    parent.removeAndUpdateSize(node);
                }
            }

            ((DefaultTreeModel) tree.getModel()).reload();

            // 恢复树节点展开状态
            UIManager.restoreExpandedStates(tree, expandedStates);
        }
    }

}