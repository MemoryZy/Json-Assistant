package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.basic.CustomizedUI;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.ui.DumbAwareActionButton;
import com.intellij.ui.treeStructure.Tree;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class CollapseAllAction extends DumbAwareActionButton {
    private final Tree tree;

    public CollapseAllAction(Tree tree, JRootPane rootPane) {
        super(JsonAssistantBundle.message("action.collapse.all.text"),
                JsonAssistantBundle.messageOnSystem("action.collapse.all.description"),
                JsonAssistantIcons.InnerAction.COLLAPSE_ALL);

        this.tree = tree;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt UP"), rootPane);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        CustomizedUI.collapseAll(tree, new TreePath(root));
    }
}