package cn.memoryzy.json.util;

import cn.memoryzy.json.constant.PluginConstant;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.Disposable;
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
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.speedSearch.ListWithFilter;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;
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

    @SuppressWarnings({"DuplicatedCode", "deprecation"})
    public static void updateListColorsScheme(JList<?> list) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color fg = ObjectUtils.chooseNotNull(scheme.getDefaultForeground(), new JBColor(UIUtil::getListForeground));
        Color bg = ObjectUtils.chooseNotNull(scheme.getDefaultBackground(), new JBColor(UIUtil::getListBackground));
        list.setForeground(fg);
        list.setBackground(bg);
    }

    public static void updateEditorColorsScheme(EditorEx editor) {
        editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
    }

    @SuppressWarnings({"DuplicatedCode", "deprecation"})
    public static void updateEditorTextFieldColorsScheme(EditorTextField editorTextField) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color fg = ObjectUtils.chooseNotNull(scheme.getDefaultForeground(), new JBColor(UIUtil::getTextFieldForeground));
        Color bg = ObjectUtils.chooseNotNull(scheme.getDefaultBackground(), new JBColor(UIUtil::getTextFieldBackground));
        editorTextField.setForeground(fg);
        editorTextField.setBackground(bg);
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

    public static void setCommentLabel(JLabel label, JCheckBox checkBox, String commentText) {
        label.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
        label.setFont(ComponentPanelBuilder.getCommentFont(label.getFont()));
        label.setBorder(getCommentBorder(checkBox));
        setCommentText(label, commentText, true, ComponentPanelBuilder.MAX_COMMENT_WIDTH);
    }

    @SuppressWarnings("SameParameterValue")
    public static void setCommentText(@NotNull JLabel component,
                                      @Nullable String commentText,
                                      boolean isCommentBelow,
                                      int maxLineLength) {
        if (commentText != null) {
            @NonNls String css = "<head><style type=\"text/css\">\n" +
                    "a, a:link {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.ENABLED) + ";}\n" +
                    "a:visited {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.VISITED) + ";}\n" +
                    "a:hover {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.HOVERED) + ";}\n" +
                    "a:active {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.PRESSED) + ";}\n" +
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

    public static Border getCommentBorder(JCheckBox checkBox) {
        Insets insets = ComponentPanelBuilder.computeCommentInsets(checkBox, true);
        insets.bottom -= 4;
        return new JBEmptyBorder(insets);
    }


    public static void controlEnableCheckBox(JCheckBox checkBox, boolean enable) {
        // 开
        if (enable) {
            if (!checkBox.isEnabled()) {
                checkBox.setEnabled(true);
            }
        } else {
            // 关
            if (checkBox.isEnabled()) {
                checkBox.setEnabled(false);
            }
        }
    }

    public static void setHelpLabel(JLabel label, String description) {
        label.setIcon(AllIcons.General.ContextHelp);
        new HelpTooltip().setDescription(description).installOn(label);
    }

    /**
     * 获取当前取得焦点的组件
     *
     * @return 组件
     */
    public static Component getFocusComponent() {
        return IdeFocusManager.getGlobalInstance().getFocusOwner();
    }

}
