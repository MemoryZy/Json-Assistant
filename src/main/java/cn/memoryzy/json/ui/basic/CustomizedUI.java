package cn.memoryzy.json.ui.basic;

import com.intellij.openapi.Disposable;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

/**
 * @author Memory
 * @since 2024/7/30
 */
public class CustomizedUI implements Disposable {

    @Override
    public void dispose() {

    }


    public static void expandAll(Tree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path);
            }
        }

        tree.expandPath(parent);
    }


    public static void collapseAll(Tree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                collapseAll(tree, path);
            }
        }

        tree.collapsePath(parent);
    }

}
