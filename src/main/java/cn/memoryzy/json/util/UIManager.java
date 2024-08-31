package cn.memoryzy.json.util;

import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/30
 */
public class UIManager implements Disposable {

    private UIManager() {
    }

    public static UIManager getInstance() {
        return new UIManager();
    }

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

    public static void addErrorBorder(JComponent component) {
        component.putClientProperty(PluginConstant.OUTLINE_PROPERTY, PluginConstant.ERROR_VALUE);
        component.repaint();
    }

    public static void addRemoveErrorListener(JTextField textField) {
        addRemoveErrorListener(textField, textField);
    }

    public static void addRemoveErrorListener(EditorTextField textField) {
        addRemoveErrorListener(textField, textField);
    }

    public static void addRemoveErrorListener(JTextField textField, JComponent target) {
        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                // 当重新有输入时，取消红色边框警告
                Object outlineValue = target.getClientProperty(PluginConstant.OUTLINE_PROPERTY);
                if (Objects.equals(outlineValue, PluginConstant.ERROR_VALUE)) {
                    target.putClientProperty(PluginConstant.OUTLINE_PROPERTY, null);
                    target.repaint();
                }
            }
        });
    }

    public static void addRemoveErrorListener(EditorTextField textField, JComponent target) {
        textField.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.@NotNull DocumentEvent event) {
                Object outlineValue = target.getClientProperty(PluginConstant.OUTLINE_PROPERTY);
                if (Objects.equals(outlineValue, PluginConstant.ERROR_VALUE)) {
                    target.putClientProperty(PluginConstant.OUTLINE_PROPERTY, null);
                    target.repaint();
                }
            }
        });
    }
}
