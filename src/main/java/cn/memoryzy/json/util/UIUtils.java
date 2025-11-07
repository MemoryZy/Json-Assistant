package cn.memoryzy.json.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.constant.ColorHolder;
import cn.memoryzy.json.constant.PathManager;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.ui.list.FilterableListWithField;
import cn.memoryzy.json.ui.tree.BaseNode;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
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
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;


/**
 * @author Memory
 * @since 2024/7/30
 */
public class UIUtils {

    private static final Logger LOG = Logger.getInstance(UIUtils.class);

    /**
     * Json 编辑器卡片名称
     */
    public static final String JSON_EDITOR_CARD_NAME = "editor";

    /**
     * Json 树卡片名称
     */
    public static final String JSON_TREE_CARD_NAME = "tree";

    /**
     * JSONQuery 卡片名
     */
    public static final String JSON_QUERY_CARD_NAME = "query";

    /**
     * JSON表格 卡片名
     */
    public static final String JSON_GRID_CARD_NAME = "grid";


    /**
     * JetBrains Maple Mono 融合字体（支持中文）
     */
    public static Font JETBRAINS_MAPLE_MONO_FONT = null;

    /**
     * JetBrains Maple Mono 融合字体名称
     */
    public static final String JETBRAINS_MAPLE_MONO_FONT_NAME = "JetBrainsMapleMono-Light.ttf";

    /**
     * 字体压缩包路径
     */
    private static final String JETBRAINS_MAPLE_MONO_FONT_ZIP_FILE_PATH = PathManager.FONTS_DIRECTORY + File.separator + "JetBrainsMapleMono-XX-NR-XX.zip";

    /**
     * JetBrains Maple Mono 融合字体路径
     */
    private static final String JETBRAINS_MAPLE_MONO_FONT_FILE_PATH = PathManager.FONTS_DIRECTORY + File.separator + JETBRAINS_MAPLE_MONO_FONT_NAME;

    /**
     * 字体压缩包的 SHA-256
     */
    private static final String EXPECTED_FONT_ZIP_HASH = "8139235ee73b71b156f764b0e23ffeb02e3fdb5cb7701216ac34284661d11e1b";

    /**
     * 字体的 SHA-256
     */
    private static final String EXPECTED_FONT_HASH = "8fc48787877be1f576c31feb50f5b20f7b0050652e5a17b459047cb21682d727";

    /**
     * 最大下载重试次数
     */
    private static final int MAX_RETRIES = 3;

    /**
     * 生成 IDE 默认编辑器组件
     *
     * @return 编辑器
     */
    public static TextEditor createDefaultTextEditor(Project project, FileType fileType, String text) {
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
            UIUtils.expandAll(tree, new TreePath(node.getPath()));
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
            UIUtils.collapseAll(tree, new TreePath(node.getPath()));
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

    public static void addRemoveErrorListener(EditorComponentImpl editorComponent) {
        addRemoveErrorListener(editorComponent, editorComponent);
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

    public static void addRemoveErrorListener(EditorComponentImpl textField, JComponent target) {
        textField.getEditor().getDocument().addDocumentListener(new DocumentListener() {
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

    public static JComponent wrapListWithFilter(@NotNull JList<? extends BaseNode> list,
                                                @Nullable Function<? super BaseNode, String> namer,
                                                boolean highlightAllOccurrences) {
        // FilterableListWithField 用于文本检索
        return FilterableListWithField.wrap(list, ScrollPaneFactory.createScrollPane(list), namer, highlightAllOccurrences);
    }

    public static void rebuildListWithFilter(JList<?> list) {
        ListWithFilter<?> listWithFilter = ComponentUtil.getParentOfType(ListWithFilter.class, list);
        if (listWithFilter != null) {
            listWithFilter.getSpeedSearch().update();
            if (list.getModel().getSize() == 0) listWithFilter.resetFilter();
        }
    }

    public static JBFont consolasFont(int size) {
        return JBUI.Fonts.create("Consolas", size);
    }

    public static JBFont consolasFont(int size, int style) {
        JBFont font = consolasFont(size);

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

    public static void expandSpecifiedLevelNode(JTree tree, int level) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        // 展开根节点，显示二级节点
        expandNode(tree, rootNode);
        if (level == 1) {
            return;
        }

        // 遍历二级节点，存在子节点时，则展开
        List<? extends TreeNode> secondLevelNodeList = JsonAssistantUtil.enumerationToList(rootNode.children());
        for (TreeNode secondLevelNode : secondLevelNodeList) {
            // 展开二级节点，显示三级节点
            expandNode(tree, (DefaultMutableTreeNode) secondLevelNode);
            if (level == 2) {
                continue;
            }

            List<? extends TreeNode> threeLevelNodeList = JsonAssistantUtil.enumerationToList(secondLevelNode.children());
            for (TreeNode threeLevelNode : threeLevelNodeList) {
                // 展开三级节点，显示四级节点
                expandNode(tree, (DefaultMutableTreeNode) threeLevelNode);
            }
        }
    }

    public static void expandNode(JTree tree, DefaultMutableTreeNode node) {
        // 创建一个TreePath，该路径从树的根节点一直到指定的node
        TreePath path = new TreePath(node.getPath());
        // 使用expandPath方法展开指定的路径
        tree.expandPath(path);
    }

    /**
     * 选中节点并滚动到可视区域
     *
     * @param node 节点
     */
    public static void selectNode(JTree tree, TreeNode node) {
        TreePath path = new TreePath(((DefaultMutableTreeNode) node).getPath());
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }


    public static JScrollPane wrapScrollPane(JComponent component) {
        if (!(component instanceof JTree || component instanceof JTable || component instanceof JList)) {
            return null;
        }

        JBScrollPane scrollPane = new JBScrollPane(component) {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferredSize = super.getPreferredSize();
                if (!isPreferredSizeSet()) {
                    setPreferredSize(new Dimension(0, preferredSize.height));
                }
                return preferredSize;
            }
        };

        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setViewportBorder(JBUI.Borders.empty());
        return scrollPane;
    }


    @Nullable
    public static Object getSelectedNode(Tree tree) {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            Object lastComponent = selectionPath.getLastPathComponent();
            if (lastComponent instanceof DefaultMutableTreeNode) {
                return lastComponent;
            }
        }
        return null;
    }

    /**
     * 获取支持中文的字体（在旧 UI 中的 List和 Tree 的高亮中，如果使用 JetBrainsMono 字体，会乱码）
     *
     * @return 字体
     */
    public static Font getChineseFont(float size) {
        return UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(size);
    }


    /**
     * 加载 JetBrains Maple Mono 字体
     */
    public static void loadAndDownloadJetbrainsMapleMonoFont() {
        // 建立一系列的父目录
        createFontsDirectories();

        // 检测字体是否存在
        File file = new File(JETBRAINS_MAPLE_MONO_FONT_FILE_PATH);
        if (FileUtil.exist(file) && EXPECTED_FONT_HASH.equals(DigestUtil.sha256Hex(file))) {
            loadJetbrainsMapleMonoFont();

        } else {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                // 定义循环初始条件
                File zipFile = new File(JETBRAINS_MAPLE_MONO_FONT_ZIP_FILE_PATH);
                boolean success = false;
                int retryCount = 0;

                // 带重试机制的下载验证流程
                while (!success && retryCount < MAX_RETRIES) {
                    try {
                        retryCount++;

                        // 通过网络请求拉取字体压缩包
                        if (!downloadFontZip(zipFile)) {
                            LOG.warn("## 字体下载失败，尝试次数: " + retryCount);
                            continue;
                        }

                        // 验证 SHA-256，防止在网络传输中出现问题
                        if (!Objects.equals(EXPECTED_FONT_ZIP_HASH, DigestUtil.sha256Hex(zipFile))) {
                            LOG.warn("## SHA-256验证失败，尝试次数: " + retryCount);
                            // 删除无效文件
                            FileUtil.del(zipFile);
                            continue;
                        }

                        // 验证成功后，解压压缩包，并加载字体文件
                        if (extractFontFromZip(zipFile)) {
                            success = true;
                            LOG.info("## 字体加载成功");
                        }

                    } catch (Exception e) {
                        LOG.warn("## 字体处理过程出错: " + e.getMessage(), e);
                        if (zipFile.exists()) {
                            FileUtil.del(zipFile);
                        }
                    }
                }

                if (success) {
                    loadJetbrainsMapleMonoFont();
                } else {
                    LOG.warn("## 字体加载失败，已达最大重试次数: " + MAX_RETRIES);
                }
            });
        }
    }

    private static boolean downloadFontZip(File outputFile) {
        String url = PlatformUtil.isChineseLocale() ? Urls.GITEE_FONT_URL : Urls.GITHUB_FONT_URL;
        // 下载并写入到指定位置
        long size = HttpUtil.downloadFile(url, outputFile);
        // 验证字节大小
        return size > 8000000;
    }

    private static boolean extractFontFromZip(File zipFile) {
        // 解压到指定目录下，不保留原压缩目录名
        try {
            // 先清空指定目录
            File file = PathManager.FONTS_DIRECTORY.toFile();
            FileUtil.clean(file);
            // 解压
            ZipUtil.unzip(zipFile, file);
        } catch (Exception e) {
            LOG.warn("## 解压失败: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    private static synchronized void loadJetbrainsMapleMonoFont() {
        // 存在则加载出来
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(JETBRAINS_MAPLE_MONO_FONT_FILE_PATH));
            // 设置默认大小
            font = font.deriveFont(13f);

            // 给 JETBRAINS_MAPLE_MONO_FONT 变量赋值
            JETBRAINS_MAPLE_MONO_FONT = font;

        } catch (Exception e) {
            LOG.warn("## 字体加载失败: " + e.getMessage(), e);
        }
    }

    private static void createFontsDirectories() {
        try {
            PathManager.createFontsDirectoriesIfNotExists();
        } catch (IOException e) {
            throw new RuntimeException("Fonts directory creation failed!", e);
        }
    }

}
