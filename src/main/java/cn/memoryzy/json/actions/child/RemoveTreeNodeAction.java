package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class RemoveTreeNodeAction extends DumbAwareAction {

    private final Tree tree;

    public RemoveTreeNodeAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.remove.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.remove.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getParent() != null) {
                    ((DefaultMutableTreeNode) node.getParent()).remove(node);
                }
            }
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }
}