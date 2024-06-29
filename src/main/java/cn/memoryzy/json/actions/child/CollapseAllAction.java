package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.TreeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.treeStructure.Tree;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class CollapseAllAction extends AnActionButton {
    private final Tree tree;

    public CollapseAllAction(Tree tree) {
        super(JsonAssistantBundle.message("action.collapse.all.text"), null, JsonAssistantIcons.InnerAction.COLLAPSE_ALL);
        this.tree = tree;

        // todo 待注册快捷键


    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        // collapseAll(new TreePath(root));
        TreeUtil.collapseAll(tree, new TreePath(root));
    }
}