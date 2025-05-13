package cn.memoryzy.json.ui.panel;

import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/12/12
 */
public class AuxiliaryTreeToolWindowPanel extends JPanel {
    private Tree tree;
    private JPanel treeComponent;

    public AuxiliaryTreeToolWindowPanel(LayoutManager layout) {
        super(layout);
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public void setTreeComponent(JPanel treeComponent) {
        this.treeComponent = treeComponent;
    }

    public Tree getTree() {
        return tree;
    }

    public JPanel getTreeComponent() {
        return treeComponent;
    }
}
