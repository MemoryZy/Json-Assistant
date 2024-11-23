package cn.memoryzy.json.ui.component;

import cn.hutool.core.util.ArrayUtil;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Memory
 * @since 2024/11/23
 */
public class PupopMenuMouseAdapter extends MouseAdapter {

    private final Tree tree;
    private final JPopupMenu popupMenu;

    public PupopMenuMouseAdapter(Tree tree, JPopupMenu popupMenu) {
        this.tree = tree;
        this.popupMenu = popupMenu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            // 获取选中的节点
            TreePath[] paths = tree.getSelectionPaths();
            if (ArrayUtil.isEmpty(paths)) {
                int row = tree.getRowForLocation(e.getX(), e.getY());
                if (row != -1) {
                    tree.setSelectionRow(row);
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            } else {
                popupMenu.show(tree, e.getX(), e.getY());
            }
        }
    }
}
