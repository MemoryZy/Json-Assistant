package cn.memoryzy.json.ui.dialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JTreeExample {
    public static void main(String[] args) {
        // 创建顶层窗口
        JFrame frame = new JFrame("JTree Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // 创建根节点
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        // 创建子节点
        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("Node 1");
        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("Node 2");

        // 创建二级节点
        DefaultMutableTreeNode node11 = new DefaultMutableTreeNode("Node 1.1");
        DefaultMutableTreeNode node12 = new DefaultMutableTreeNode("Node 1.2");
        DefaultMutableTreeNode node21 = new DefaultMutableTreeNode("Node 2.1");
        DefaultMutableTreeNode node22 = new DefaultMutableTreeNode("Node 2.2");

        // 添加二级节点到一级节点
        node1.add(node11);
        node1.add(node12);
        node2.add(node21);
        node2.add(node22);

        // 添加一级节点到根节点
        root.add(node1);
        root.add(node2);

        // 创建 JTree 并设置模型
        final JTree jtree = new JTree(root);

        // 隐藏根节点
        jtree.setRootVisible(false);

        // 将 JTree 添加到滚动面板
        JScrollPane scrollPane = new JScrollPane(jtree);

        // 创建按钮来控制删除节点
        JButton deleteNodeButton = new JButton("Delete Node 1.1");

        // 添加按钮点击事件监听器
        deleteNodeButton.addActionListener(e -> {
            System.out.println("Delete Node 1.1 button clicked");
            deleteNode(jtree, node11);
        });

        // 创建面板来放置按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteNodeButton);

        // 将滚动面板和按钮面板添加到框架
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // 显示窗口
        frame.setVisible(true);
    }

    // 删除节点的方法
    public static void deleteNode(JTree tree, DefaultMutableTreeNode nodeToDelete) {
        if (nodeToDelete == null) {
            return;
        }

        // 保存当前的展开状态
        Map<TreePath, Boolean> expandedStates = saveExpandedStates(tree);

        // 找到要删除节点的父节点
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodeToDelete.getParent();
        if (parent != null) {
            parent.remove(nodeToDelete);
            tree.getModel().valueForPathChanged(new TreePath(parent.getPath()), parent);
        }

        // 恢复展开状态
        restoreExpandedStates(tree, expandedStates);
    }

    // 保存当前的展开状态
    public static Map<TreePath, Boolean> saveExpandedStates(JTree tree) {
        Map<TreePath, Boolean> expandedStates = new HashMap<>();
        Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        if (expandedPaths != null) {
            while (expandedPaths.hasMoreElements()) {
                TreePath path = expandedPaths.nextElement();
                expandedStates.put(path, true);
            }
        }
        return expandedStates;
    }

    // 恢复展开状态
    public static void restoreExpandedStates(JTree tree, Map<TreePath, Boolean> expandedStates) {
        for (Map.Entry<TreePath, Boolean> entry : expandedStates.entrySet()) {
            if (entry.getValue()) {
                tree.expandPath(entry.getKey());
            }
        }
    }
}