package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.tree.JsonFilterableTree;
import cn.memoryzy.json.ui.tree.JsonTreeNode2;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Map;

public class RemoveTreeNodeAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;
    private final JsonFilterableTree filterableTree;

    public RemoveTreeNodeAction(Tree tree, JsonFilterableTree filterableTree) {
        super(JsonAssistantBundle.messageOnSystem("action.structure.remove.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.remove.description"),
                null);
        this.tree = tree;
        this.filterableTree = filterableTree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            // 记录树节点展开状态
            Map<TreePath, Boolean> expandedStates = UIUtils.recordExpandedStates(tree);

            for (TreePath path : paths) {
                JsonTreeNode2 node = JsonFilterableTree.getNode(path);
                JsonTreeNode2 parent = node.getParent();
                if (parent != null) {
                    parent.removeAndUpdateSize(node);
                }
            }

            filterableTree.updateStructure();

            // 恢复树节点展开状态
            UIUtils.restoreExpandedStates(tree, expandedStates);
        }
    }

}