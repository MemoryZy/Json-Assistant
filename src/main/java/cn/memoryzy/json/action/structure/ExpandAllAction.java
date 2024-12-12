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

public class ExpandAllAction extends DumbAwareActionButton {

    /**
     * 树
     */
    private final Tree tree;

    /**
     * 是否包含根节点，如果为 true，则展开所有节点，否则只展开二级及以下节点
     */
    private final boolean includeRoot;

    public ExpandAllAction(Tree tree, JComponent component, boolean includeRoot) {
        super(JsonAssistantBundle.message("action.expand.all.text"),
                JsonAssistantBundle.messageOnSystem("action.expand.all.description"),
                JsonAssistantIcons.Structure.INTELLIJ_EXPAND_ALL);
        this.tree = tree;
        this.includeRoot = includeRoot;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt DOWN"), component);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        if (includeRoot) {
            UIManager.expandAll(tree, new TreePath(root));
        } else {
            UIManager.expandSecondaryNode(tree, root);
        }
    }
}