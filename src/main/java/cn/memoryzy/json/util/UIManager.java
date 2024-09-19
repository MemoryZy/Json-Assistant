package cn.memoryzy.json.util;

import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
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

    /**
     * 生成 IDE 默认编辑器组件
     *
     * @return 编辑器
     */
    public static TextEditor createDefaultTextEditor(Project project, FileType fileType, String text) {
        // TODO 是否需要创建物理文件，以供其他插件功能使用，例如 Json To Table
        LightVirtualFile lightVirtualFile = new LightVirtualFile("Dummy." + fileType.getDefaultExtension(), fileType, text);
        return (TextEditor) TextEditorProvider.getInstance().createEditor(project, lightVirtualFile);
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
