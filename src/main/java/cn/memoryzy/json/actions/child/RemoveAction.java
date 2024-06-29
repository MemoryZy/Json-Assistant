package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class RemoveAction extends AnAction {
    private final Tree tree;

    public RemoveAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.remove.text"));
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