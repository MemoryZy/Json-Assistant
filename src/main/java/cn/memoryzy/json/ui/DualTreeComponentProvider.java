package cn.memoryzy.json.ui;

import cn.memoryzy.json.model.StructureConfig;
import cn.memoryzy.json.model.wrapper.JsonWrapper;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/2/19
 */
public class DualTreeComponentProvider {

    private final JsonStructureComponentProvider leftProvider;
    private final JsonStructureComponentProvider rightProvider;

    public DualTreeComponentProvider(JsonWrapper leftObj, JsonWrapper rightObj) {
        this.leftProvider = new JsonStructureComponentProvider(leftObj, null, StructureConfig.of(true, false));
        this.rightProvider = new JsonStructureComponentProvider(rightObj, null, StructureConfig.of(true, false));
    }

    public JComponent createComponent() {
        // 1行2列
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 0));

        // 将滚动面板添加到主面板中
        panel.add(leftProvider.getTreeComponent());
        panel.add(rightProvider.getTreeComponent());

        return panel;
    }

    // 辅助方法：递归添加子节点
    private static void addNodes(DefaultMutableTreeNode node, String nodeName, int depth) {
        if (depth == 0) return;

        for (int i = 1; i <= 3; i++) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(nodeName + " " + i);
            node.add(child);
            addNodes(child, nodeName + "." + i, depth - 1);
        }
    }
}
