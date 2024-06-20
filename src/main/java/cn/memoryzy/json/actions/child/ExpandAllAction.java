package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.icons.JsonAssistantIcons;
import cn.memoryzy.json.utils.TreeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ExpandAllAction extends AnActionButton {
    private final Tree tree;

    public ExpandAllAction(Tree tree) {
        super(JsonAssistantBundle.message("action.expand.all.text"), null, JsonAssistantIcons.InnerAction.EXPAND_ALL);
        this.tree = tree;

        // todo 待注册快捷键
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        TreeUtil.expandAll(tree, new TreePath(root));
    }
}