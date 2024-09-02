package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.UIManager;
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
                JsonAssistantIcons.Structure.INTELLIJ_COLLAPSE_ALL);

        this.tree = tree;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt UP"), rootPane);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        UIManager.collapseAll(tree, new TreePath(root));
    }
}