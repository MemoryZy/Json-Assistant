package cn.memoryzy.json.ui.component;

import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/12
 */
public class AuxiliaryTreeToolWindowPanel extends JPanel {
    private final Tree tree;
    private final JPanel treeComponent;

    public AuxiliaryTreeToolWindowPanel(LayoutManager layout, Tree tree, JPanel treeComponent) {
        super(layout);
        this.tree = tree;
        this.treeComponent = treeComponent;
    }

    public Tree getTree() {
        return tree;
    }

    public JPanel getTreeComponent() {
        return treeComponent;
    }
}
