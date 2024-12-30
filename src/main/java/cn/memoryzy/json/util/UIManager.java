package cn.memoryzy.json.util;

import cn.memoryzy.json.constant.ColorHolder;
import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.panel.ComponentPanelBuilder;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.speedSearch.ListWithFilter;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Function;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author Memory
 * @since 2024/7/30
 */
public class UIManager implements Disposable {

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

    /**
     * 展开二级节点（适用于root节点隐藏的情况）
     *
     * @param tree 树
     */
    public static void expandSecondaryNode(Tree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandSecondaryNode(tree, root);
    }

    /**
     * 展开二级节点（适用于root节点隐藏的情况）
     *
     * @param tree 树
     * @param root 根节点
     */
    public static void expandSecondaryNode(Tree tree, TreeNode root) {
        // 展开二级节点
        for (Enumeration<?> e = root.children(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            UIManager.expandAll(tree, new TreePath(node.getPath()));
        }
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

    /**
     * 折叠二级节点（适用于root节点隐藏的情况）
     *
     * @param tree 树
     */
    public static void collapseSecondaryNode(Tree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        collapseSecondaryNode(tree, root);
    }

    /**
     * 折叠二级节点（适用于root节点隐藏的情况）
     *
     * @param tree 树
     * @param root 根节点
     */
    public static void collapseSecondaryNode(Tree tree, TreeNode root) {
        // 折叠二级节点
        for (Enumeration<?> e = root.children(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            UIManager.collapseAll(tree, new TreePath(node.getPath()));
        }
    }

    /**
     * 记录树的节点展开状态
     *
     * @param tree 树实例
     * @return 包含节点路径和展开状态的 Map
     */
    public static Map<TreePath, Boolean> recordExpandedStates(JTree tree) {
        Map<TreePath, Boolean> expandedStates = new HashMap<>();
        Enumeration<TreePath> paths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        if (paths != null) {
            while (paths.hasMoreElements()) {
                TreePath path = paths.nextElement();
                expandedStates.put(path, true);
            }
        }
        return expandedStates;
    }

    /**
     * 恢复树的节点展开状态
     *
     * @param tree           树实例
     * @param expandedStates 包含节点路径和展开状态的 Map
     */
    public static void restoreExpandedStates(JTree tree, Map<TreePath, Boolean> expandedStates) {
        for (Map.Entry<TreePath, Boolean> entry : expandedStates.entrySet()) {
            if (entry.getValue()) {
                tree.expandPath(entry.getKey());
            }
        }
    }

    public static void addErrorBorder(JComponent component) {
        // 这行的作用是给文本框外部变为红色
        component.putClientProperty(PluginConstant.OUTLINE_PROPERTY, PluginConstant.ERROR_VALUE);
        // 重新计算布局
        component.revalidate();
        // 重新渲染组件
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
                    target.revalidate();
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
                    target.revalidate();
                    target.repaint();
                }
            }
        });
    }

    public static void updateComponentColorsScheme(JComponent component) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        component.setForeground(scheme.getDefaultForeground());
        component.setBackground(scheme.getDefaultBackground());
    }

    public static void updateEditorColorsScheme(EditorEx editor) {
        editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
    }

    public static <T> JComponent wrapListWithFilter(@NotNull JList<? extends T> list,
                                                    @Nullable Function<? super T, String> namer,
                                                    boolean highlightAllOccurrences) {
        // ListWithFilter 用于文本检索
        return ListWithFilter.wrap(list, ScrollPaneFactory.createScrollPane(list), namer, highlightAllOccurrences);
    }

    public static JBFont consolasFont(int size) {
        return JBUI.Fonts.create("Consolas", size);
    }

    public static JBFont jetBrainsMonoFont(int size) {
        return JBUI.Fonts.create("JetBrains Mono", size);
    }

    public static JBFont microsoftYaHeiUIFont(int size) {
        return JBUI.Fonts.create("Microsoft YaHei UI", size);
    }

    public static JBFont microsoftYaHeiUIFont(int size, int style) {
        JBFont font = JBUI.Fonts.create("Microsoft YaHei UI", size);
        switch (style) {
            case Font.BOLD:
                font = font.asBold();
                break;
            case Font.ITALIC:
                font = font.asItalic();
                break;
        }

        return font;
    }

    public static void setCommentLabel(JLabel label, JComponent component, String commentText) {
        label.setBorder(getCommentBorder(component));
        setCommentLabel(label, commentText);
    }

    public static void setCommentLabel(JLabel label, String commentText) {
        label.setForeground(UIUtil.getContextHelpForeground());
        label.setFont(getCommentFont(label.getFont()));
        setCommentText(label, commentText, true, 70);
    }

    @SuppressWarnings("SameParameterValue")
    public static void setCommentText(@NotNull JLabel component,
                                      @Nullable String commentText,
                                      boolean isCommentBelow,
                                      int maxLineLength) {
        if (commentText != null) {
            @NonNls String css = "<head><style type=\"text/css\">\n" +
                    "a, a:link {color:#" + ColorUtil.toHex(ColorHolder.Foreground.ENABLED) + ";}\n" +
                    "a:visited {color:#" + ColorUtil.toHex(ColorHolder.Foreground.VISITED) + ";}\n" +
                    "a:hover {color:#" + ColorUtil.toHex(ColorHolder.Foreground.HOVERED) + ";}\n" +
                    "a:active {color:#" + ColorUtil.toHex(ColorHolder.Foreground.PRESSED) + ";}\n" +
                    //"body {background-color:#" + ColorUtil.toHex(JBColor.YELLOW) + ";}\n" + // Left for visual debugging
                    "</style>\n</head>";
            HtmlChunk text = HtmlChunk.raw(commentText);
            if (maxLineLength > 0 && commentText.length() > maxLineLength && isCommentBelow) {
                int width = component.getFontMetrics(component.getFont()).stringWidth(commentText.substring(0, maxLineLength));
                text = text.wrapWith(HtmlChunk.div().attr("width", width));
            } else {
                text = text.wrapWith(HtmlChunk.div());
            }
            component.setText(new HtmlBuilder()
                    .append(HtmlChunk.raw(css))
                    .append(text.wrapWith("body"))
                    .wrapWith("html")
                    .toString());
        }
    }

    public static Border getCommentBorder(JComponent component) {
        Insets insets = ComponentPanelBuilder.computeCommentInsets(component, true);
        insets.bottom -= 4;
        return new JBEmptyBorder(insets);
    }


    public static void controlEnableCheckBox(JCheckBox checkBox, boolean enable) {
        controlEnableToggleButton(checkBox, enable);
    }

    public static void controlEnableRadioButton(JRadioButton radioButton, boolean enable) {
        controlEnableToggleButton(radioButton, enable);
    }

    public static void controlEnableToggleButton(JToggleButton toggleButton, boolean enable) {
        // 开
        if (enable) {
            if (!toggleButton.isEnabled()) {
                toggleButton.setEnabled(true);
            }
        } else {
            // 关
            if (toggleButton.isEnabled()) {
                toggleButton.setEnabled(false);
            }
        }
    }

    public static void setHelpLabel(JLabel label, String description) {
        label.setIcon(AllIcons.General.ContextHelp);
        new HelpTooltip().setDescription(description).installOn(label);
    }

    /**
     * 获取当前取得焦点的组件(需要在EDT线程内执行（例如Action.actionPerformed内）)
     *
     * @return 组件
     */
    public static Component getFocusComponent() {
        Component component = IdeFocusManager.getGlobalInstance().getFocusOwner();
        return Objects.nonNull(component) ? component : null;
    }

    public static @Nullable JComponent getWindowComponent(Project project) {
        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);
        return window != null ? window.getComponent() : null;
    }

    // public static @Nullable Component getFocusedComponent() {
    //     WindowManager windowManager = WindowManager.getInstance();
    //
    //     Window activeWindow = windowManager.getMostRecentFocusedWindow();
    //     if (activeWindow == null) {
    //         activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    //         if (activeWindow == null) {
    //             activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
    //             if (activeWindow == null) return null;
    //         }
    //     }
    //
    //     // In case we have an active floating toolwindow and some component in another window focused,
    //     // we want this other component to receive key events.
    //     // Walking up the window ownership hierarchy from the floating toolwindow would have led us to the main IdeFrame
    //     // whereas we want to be able to type in other frames as well.
    //     if (activeWindow instanceof FloatingDecoratorMarker) {
    //         IdeFocusManager ideFocusManager = IdeFocusManager.findInstanceByComponent(activeWindow);
    //         IdeFrame lastFocusedFrame = ideFocusManager.getLastFocusedFrame();
    //         JComponent frameComponent = lastFocusedFrame != null ? lastFocusedFrame.getComponent() : null;
    //         Window lastFocusedWindow = frameComponent != null ? SwingUtilities.getWindowAncestor(frameComponent) : null;
    //         boolean toolWindowIsNotFocused = windowManager.getFocusedComponent(activeWindow) == null;
    //         if (toolWindowIsNotFocused && lastFocusedWindow != null) {
    //             activeWindow = lastFocusedWindow;
    //         }
    //     }
    //
    //     // try to find first parent window that has focus
    //     Window window = activeWindow;
    //     Component focusedComponent = null;
    //     while (window != null) {
    //         focusedComponent = windowManager.getFocusedComponent(window);
    //         if (focusedComponent != null) {
    //             break;
    //         }
    //         window = window.getOwner();
    //     }
    //     if (focusedComponent == null) {
    //         focusedComponent = activeWindow;
    //     }
    //
    //     return focusedComponent;
    // }

    /**
     * 获取注释字体
     *
     * @param font 字体
     * @return 字体
     */
    public static Font getCommentFont(Font font) {
        return new FontUIResource(font.deriveFont((float) (font.getSize() - 0.7)));
    }


    /**
     * 重绘编辑器
     *
     * @param editor 编辑器
     */
    public static void repaintEditor(Editor editor) {
        repaintComponent(editor.getComponent());
    }

    /**
     * 重绘组件
     *
     * @param component 组件
     */
    public static void repaintComponent(JComponent component) {
        component.revalidate();
        component.repaint();
    }

    public static void setText(JComponent textField, String text) {
        if (textField instanceof JTextField) {
            ((JTextField) textField).setText(text);
        } else if (textField instanceof EditorTextField) {
            ((EditorTextField) textField).setText(text);
        }
    }

}
