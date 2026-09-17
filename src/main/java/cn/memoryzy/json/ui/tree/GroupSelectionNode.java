package cn.memoryzy.json.ui.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Memory
 * @since 2025/9/15
 */
public class GroupSelectionNode extends DefaultMutableTreeNode {

    /**
     * 节点名
     */
    private String name;

    /**
     * 节点图标
     */
    private Icon icon;

    /**
     * 关联的原始NoteNode，如果是叶子节点
     */
    private HistoryNode originalNode;

    /**
     * 节点在树中的深度
     */
    private int depth;


    public GroupSelectionNode(String name) {
        this.name = name;
    }

    /**
     * 在当前节点的子节点中查找指定名称的节点
     *
     * @param name 节点名称
     * @return 找到的节点，未找到返回null
     */
    public GroupSelectionNode findChildByName(String name) {
        ArrayList<TreeNode> children = Collections.list(children());
        for (TreeNode child : children) {
            GroupSelectionNode childNode = (GroupSelectionNode) child;
            if (childNode.getName().equals(name)) {
                return childNode;
            }
        }

        return null;
    }


    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }

    public HistoryNode getOriginalNode() {
        return originalNode;
    }

    public int getDepth() {
        return depth;
    }

    public GroupSelectionNode setName(String name) {
        this.name = name;
        return this;
    }

    public GroupSelectionNode setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public GroupSelectionNode setOriginalNode(HistoryNode originalNode) {
        this.originalNode = originalNode;
        return this;
    }

    public GroupSelectionNode setDepth(int depth) {
        this.depth = depth;
        return this;
    }
}
