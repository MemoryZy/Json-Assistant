package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.basic.CustomizedUI;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.treeStructure.Tree;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ExpandAllAction extends AnActionButton {
    private final Tree tree;

    public ExpandAllAction(Tree tree, JRootPane rootPane) {
        super(JsonAssistantBundle.message("action.expand.all.text"),
                JsonAssistantBundle.messageOnSystem("action.expand.all.description"),
                JsonAssistantIcons.InnerAction.EXPAND_ALL);
        this.tree = tree;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt DOWN"), rootPane);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        CustomizedUI.expandAll(tree, new TreePath(root));
    }
}