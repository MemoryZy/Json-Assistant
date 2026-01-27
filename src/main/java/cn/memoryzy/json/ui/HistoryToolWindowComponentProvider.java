package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.notification.ExpiringNotificationAction;
import cn.memoryzy.json.action.toolwindow.history.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ToolWindowConstant;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.enums.HistoryAffectType;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.event.NavigateRecordEvent;
import cn.memoryzy.json.event.RefreshHistoryTreeEvent;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.BaseData;
import cn.memoryzy.json.service.persistent.state.JsonGroup;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.dialog.TargetGroupSelectionDialog;
import cn.memoryzy.json.ui.listener.TreeRightClickPopupMenuMouseAdapter;
import cn.memoryzy.json.ui.panel.CreateWithTemplatesDialogPanel;
import cn.memoryzy.json.ui.panel.NewItemPopupPanel;
import cn.memoryzy.json.ui.tree.HistoryFilterableTree;
import cn.memoryzy.json.ui.tree.HistoryNode;
import cn.memoryzy.json.ui.tree.HistoryTreeRenderer;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/8/8
 */
public class HistoryToolWindowComponentProvider implements Disposable {

    private static final Logger LOG = Logger.getInstance(HistoryToolWindowComponentProvider.class);

    public static final Key<VirtualFile> FILE_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".HistorySourceFile");

    /**
     * 分割比例持久化
     */
    public static final String SPLITTER_PROPORTION_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".HistorySplitterProportionKey";

    /**
     * 3分钟
     */
    private static final int EDITOR_CLOSE_DELAY_MS = 3 * 60 * 1000;

    /**
     * 记录/分组 名称限制长度
     */
    private static final int NAME_LENGTH_LIMIT = 300;


    private final Project project;
    private final ToolWindowEx toolWindow;
    private final SimpleToolWindowPanel windowPanel;
    /**
     * 核心：单个 Alarm 实例，作为中央调度器
     */
    private final Alarm centralAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

    // -------------------------- left
    private Tree tree;
    private HistoryFilterableTree filterableTree;
    private SearchTextField searchTextField;

    // -------------------------- right
    private JBPanelWithEmptyText editorPanel;
    private JPanel toolbarPanel;
    /**
     * 当前显示的编辑器实例
     */
    private Editor activeEditor;
    /**
     * 当前显示的记录项
     */
    private HistoryNode currentRecord;
    /**
     * 编辑器池
     */
    private final Map<HistoryNode, Editor> editorPool = new ConcurrentHashMap<>();

    /**
     * 记录每个记录对应的延迟任务Runnable对象
     */
    private final Map<HistoryNode, Runnable> scheduledTasks = new ConcurrentHashMap<>();

    private final EditorFactory editorFactory = EditorFactory.getInstance();
    private final HistoryManager historyManager;

    /**
     * 消息总线（项目级）
     */
    private final MessageBusConnection projectConnection;

    public HistoryToolWindowComponentProvider(Project project, ToolWindowEx toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.historyManager = HistoryManager.getInstance(project);
        this.windowPanel = new SimpleToolWindowPanel(false, false);
        this.projectConnection = project.getMessageBus().connect(historyManager);
    }

    public JComponent createComponent() {
        JBSplitter splitter = new JBSplitter(false, 0.3f);
        splitter.setAndLoadSplitterProportionKey(SPLITTER_PROPORTION_KEY);
        splitter.setFirstComponent(createLeftComponent());
        splitter.setSecondComponent(createRightComponent());

        registerEventHandlers();

        windowPanel.setContent(splitter);
        return windowPanel;
    }

    private JComponent createLeftComponent() {
        // -------------------------- left
        HistoryNode rootNode = new HistoryNode(JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.root.text"))
                .setNodeType(HistoryTreeNodeType.ROOT)
                .setNodeIcon(AllIcons.Nodes.ModuleGroup)
                .setPath(JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.root.text"));

        this.filterableTree = new HistoryFilterableTree(project, rootNode, this);
        this.tree = filterableTree.getTree();
        this.searchTextField = filterableTree.installSearchField();

        // 如果有节点，就设置根节点可见；没有的话，就设置不可见
        DefaultMutableTreeNode root = filterableTree.getRoot();
        if (root.getChildCount() > 0) {
            if (!tree.isRootVisible()) {
                tree.setRootVisible(true);
            }
        } else {
            if (tree.isRootVisible()) {
                tree.setRootVisible(false);
            }
        }

        int fontSize = JBUIScale.scaleFontSize(13);
        JBFont jbFont = UIUtils.jetBrainsMonoFont(fontSize);
        Font font = UIUtils.JETBRAINS_MAPLE_MONO_FONT;

        if (null != font) {
            font = font.deriveFont((float) fontSize);
        } else {
            // 先设置 JetBrains Mono，在搜索时，切换为
            font = jbFont;
        }

        tree.setFont(font);
        tree.setCellRenderer(new HistoryTreeRenderer(fontSize));
        tree.setExpandableItemsEnabled(true);

        // 增加拖动效果
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        // tree.setTransferHandler(new NoteTransferHandler(this));

        setEmptyTextForNoElements(tree.getEmptyText());
        tree.addMouseListener(new TreeRightClickPopupMenuMouseAdapter(tree, createRightMousePopupMenu()));
        tree.addTreeSelectionListener(this::onNodeSelected);

        // 焦点切换到编辑器事件
        DumbAwareAction.create(event -> requestFocusOnEditor(event.getProject()))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ESCAPE"), tree);

        return new BorderLayoutPanel()
                .addToTop(new BorderLayoutPanel().addToTop(searchTextField).addToCenter(new JSeparator()))
                .addToCenter(UIUtils.wrapScrollPane(tree));
    }

    private JComponent createRightComponent() {
        this.toolbarPanel = createToolbarPanel();
        this.editorPanel = new JBPanelWithEmptyText(new BorderLayout());

        this.editorPanel.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("toolwindow.history.editor.panel.welcome.empty.text"));
        this.toolbarPanel.setVisible(false);
        return new BorderLayoutPanel().addToCenter(editorPanel).addToRight(toolbarPanel);
    }

    private JPanel createToolbarPanel() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new OpenHistoryInEditorAction(this, toolWindow));
        group.add(Separator.create());
        group.add(new ToggleUseSoftWrapsHistoryAction(this));
        group.add(new ScrollToTheEndHistoryAction(this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, false);
        toolbar.setOrientation(SwingConstants.VERTICAL);
        toolbar.setTargetComponent(editorPanel);

        return new BorderLayoutPanel().addToCenter(toolbar.getComponent());
    }

    /**
     * 注册事件处理器
     */
    private void registerEventHandlers() {
        projectConnection.subscribe(RefreshHistoryTreeEvent.TOPIC, (RefreshHistoryTreeEvent) this::refreshHistoryTree);
        // 注册精确找到记录事件
        projectConnection.subscribe(NavigateRecordEvent.TOPIC, (NavigateRecordEvent) this::navigateRecord);
    }


    /**
     * 用户选中记录时的切入点处理
     * <p>这是最主要的业务逻辑入口，负责编辑器切换和生命周期管理</p>
     */
    public void onNodeSelected(TreeSelectionEvent e) {
        HistoryNode historyNode = filterableTree.getUserObject((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());

        // 在批量删除节点时，FilterableTree 的 updateStructure 方法的 removeNodeFromParent 会有一个选中已删除的节点的操作，这会触发 `源文件丢失` 的提示
        // 所以在此加上一个是否被删除的判断，只针对记录节点
        if (null != historyNode && historyNode.isRecord()) {
            if (historyManager.containsRecord(historyNode.getId())) {
                showRecordContent(project, historyNode);
            }
        } else {
            // 取消编辑器的显示
            refreshEditorPanel(null);
            clearCurrentState();
            // 将上一份记录对应的编辑器加入关闭队列
            HistoryNode node = filterableTree.getUserObject(e.getOldLeadSelectionPath());
            if (null != node && node.isRecord()) {
                scheduleCloseForRecord(node);
            }
        }
    }

    private void showRecordContent(Project project, HistoryNode selectedNode) {
        // 1. 如果选中的是当前记录，不做任何操作
        if (selectedNode.equals(this.currentRecord)) return;

        // 2. 如果之前有选中的记录，为其安排延迟关闭
        if (null != this.currentRecord) scheduleCloseForRecord(currentRecord);

        // 取消新选中记录的待关闭任务，确保新选中的记录不会被错误关闭（如果之前安排了关闭）
        cancelPendingCloseForRecord(selectedNode);

        // 更新当前记录引用
        this.currentRecord = selectedNode;

        // 获取对应的文件类型
        JsonRecord record = (JsonRecord) selectedNode.getValue();
        VirtualFile sourceFile = record.getSourceFile();

        if (!PlatformUtil.isValidFile(sourceFile)) {
            NotificationAction createAction = new ExpiringNotificationAction(
                    JsonAssistantBundle.messageOnSystem("action.create.file.text"),
                    () -> newRecordFileNotification(record, selectedNode));
            NotificationAction removeAction = new ExpiringNotificationAction(
                    JsonAssistantBundle.messageOnSystem("action.delete.record.text"),
                    (event, notification) -> removeRecordNotification(event, record));

            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notification.record.lost.content", record.getName()), NotificationType.WARNING, List.of(createAction, removeAction), project);
            refreshEditorPanel(null);
            return;
        }

        setupEditorContent(selectedNode, sourceFile);
    }

    private void setupEditorContent(HistoryNode selectedNode, VirtualFile sourceFile) {
        // 创建新的编辑器，或者在该记录对应的编辑器未清除前继续复用
        Editor editor = editorPool.computeIfAbsent(selectedNode, node -> createEditorForType(project, sourceFile));
        VirtualFile file = editor.getUserData(FILE_KEY);
        if (null == file || !Objects.equals(sourceFile, file)) {
            editor.putUserData(FILE_KEY, sourceFile);
        }

        // 更新UI显示
        if (!editor.equals(activeEditor)) {
            refreshEditorPanel(editor.getComponent());
        }

        this.activeEditor = editor;
    }


    private Editor createEditorForType(Project project, VirtualFile file) {
        EditorEx editor = (EditorEx) PlatformUtil.createEditor(project, file, false, EditorKind.MAIN_EDITOR);
        configureEditor(editor);
        return editor;
    }

    private void configureEditor(EditorEx editor) {
        EditorSettings settings = editor.getSettings();
        // 行号显示
        settings.setLineNumbersShown(true);
        // 设置显示的缩进导轨
        settings.setIndentGuidesShown(true);
        // 折叠块显示
        settings.setFoldingOutlineShown(false);
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(true);

        ErrorStripeEditorCustomization.DISABLED.customize(editor);
        Objects.requireNonNull(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization()).customize(editor);

        // 设置绘画背景
        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        gutterComponentEx.setPaintBackground(false);
        editor.setBorder(JBUI.Borders.empty());

        JComponent component = editor.getComponent();
        component.setFont(UIUtils.consolasFont(15));

        // 切换软换行状态
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(ToolWindowConstant.History.SOFT_WRAPS_HISTORY_SELECT_STATE);
        if (null != value) {
            AbstractToggleUseSoftWrapsAction.toggleSoftWraps(editor, null, Boolean.parseBoolean(value));
        }

        // 焦点切换到树事件
        DumbAwareAction.create(event -> requestFocusOnTree(event.getProject()))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("alt 1"), editor.getComponent());
    }


    private JPopupMenu createRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.addSeparator();
        group.add(new NewHistoryGroup(this, windowPanel));
        group.addSeparator();
        group.add(new RenameHistoryAction(this, windowPanel));
        group.addSeparator();
        group.add(new RemoveHistoryAction(this, windowPanel));
        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        return actionPopupMenu.getComponent();
    }

    /**
     * 为指定记录安排延迟关闭任务
     * <p>在指定延迟时间后自动关闭对应的编辑器以释放资源</p>
     *
     * @param historyNode 需要安排关闭的记录
     */
    private void scheduleCloseForRecord(@NotNull HistoryNode historyNode) {
        // 检查编辑器池中是否还存在该记录的编辑器，如果编辑器已不存在，则无需安排关闭
        if (!editorPool.containsKey(historyNode)) return;

        // 取消该记录可能已存在的旧定时任务，避免重复安排任务（这样不会和下方closeTask任务中的remove冲突，因为任务是定时执行）
        cancelPendingCloseForRecord(historyNode);

        // 创建新的延迟关闭任务
        Runnable closeTask = () -> {
            // 任务执行时，从映射表中移除自身记录
            scheduledTasks.remove(historyNode);
            // 执行编辑器关闭逻辑
            closeEditor(historyNode);
        };

        // 将新任务记录到映射表中
        scheduledTasks.put(historyNode, closeTask);
        // 向中央Alarm提交延迟任务
        centralAlarm.addRequest(closeTask, EDITOR_CLOSE_DELAY_MS);
    }

    /**
     * 取消指定记录的待关闭任务
     * 当用户重新选择记录时调用，避免编辑器被误关闭
     *
     * @param historyNode 需要取消关闭任务的记录文件
     */
    private void cancelPendingCloseForRecord(@NotNull HistoryNode historyNode) {
        Runnable task = scheduledTasks.remove(historyNode);
        if (task != null) {
            centralAlarm.cancelRequest(task);
        }
    }

    /**
     * 立即关闭并释放指定记录的编辑器
     * 执行实际的编辑器资源释放操作
     *
     * @param historyNode 需要关闭的记录
     */
    private void closeEditor(@NotNull HistoryNode historyNode) {
        // 从编辑器池中移除记录对应的编辑器
        Editor editor = editorPool.remove(historyNode);
        // 编辑器已不存在，直接返回
        if (editor == null) return;

        // 确保从定时任务映射表中也移除记录
        scheduledTasks.remove(historyNode);

        if (!editor.isDisposed()) {
            ApplicationManager.getApplication().invokeLater(() -> editorFactory.releaseEditor(editor));
        }
    }

    public void refreshEditorPanel(@Nullable Component component) {
        editorPanel.removeAll();
        if (null != component) {
            editorPanel.add(component, BorderLayout.CENTER);
            if (!toolbarPanel.isVisible()) toolbarPanel.setVisible(true);
        } else {
            if (toolbarPanel.isVisible()) toolbarPanel.setVisible(false);
        }

        editorPanel.revalidate();
        editorPanel.repaint();
    }

    public void clearCurrentState() {
        activeEditor = null;
        currentRecord = null;
    }

    public void setEmptyTextForNoElements(StatusText emptyText) {
        emptyText.appendText(JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.empty.text"));
        emptyText.appendLine("");
        emptyText.appendLine(AllIcons.Actions.ModuleDirectory, "  " + JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.addGroup.empty.text"), SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 添加目录
                executeAddGroup();
            }
        });

        emptyText.appendLine(AllIcons.FileTypes.Text, "  " + JsonAssistantBundle.messageOnSystem("toolwindow.history.tree.addRecord.empty.text"), SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 添加记录
                executeAddRecord();
            }
        });
    }

    public List<HistoryNode> getSelectedRecordNodes() {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (ArrayUtil.isEmpty(selectionPaths)) return new ArrayList<>();
        // 转换为自定义节点
        return Arrays.stream(selectionPaths).map(filterableTree::getUserObject).collect(Collectors.toList());
    }

    public List<HistoryNode> getSelectedRecordNodes(List<DefaultMutableTreeNode> nodeList) {
        // 转换为自定义节点
        return nodeList.stream().map(filterableTree::getUserObject).collect(Collectors.toList());
    }

    public List<DefaultMutableTreeNode> getSelectedNodes() {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (ArrayUtil.isEmpty(selectionPaths)) return new ArrayList<>();
        // 转换为自定义节点
        return Arrays.stream(selectionPaths).map(path -> (DefaultMutableTreeNode) path.getLastPathComponent()).collect(Collectors.toList());
    }

    /**
     * 刷新树（除了事件订阅能使用此方法，其余的都不能）
     *
     * @param sourceProject 源项目
     * @param affectType    操作类型
     * @param newNodeId     新节点ID
     */
    private void refreshHistoryTree(Project sourceProject, HistoryAffectType affectType, @Nullable String newNodeId) {
        searchTextField.setText(null);

        // 是否为相同项目，如果是相同项目，则修改选中；如果非相同项目，则原来的选中状态不变
        boolean isSameProject = Objects.equals(sourceProject, this.project);

        // 保存当前选中节点和展开状态（基于内容而非节点对象）
        List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(tree);
        // 记录当前选中节点
        DefaultMutableTreeNode currentSelection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        // 记录当前选中节点的 ID 信息（用于恢复）
        String selectedNodeId = null;
        // 记录父节点ID和兄弟节点信息，用于REMOVE操作后的选择
        String parentNodeId = null;
        // 保存兄弟节点的ID列表，用于查找前后节点
        List<String> siblingNodeIds = new ArrayList<>();
        // 记录当前选中节点在父节点中的索引
        int selectedNodeIndexInParent = -1;

        if (currentSelection != null) {
            HistoryNode historyNode = filterableTree.getUserObject(currentSelection);
            if (historyNode != null && historyNode.isNotRoot()) {
                selectedNodeId = historyNode.getId();

                // --- 新增：在树更新前，获取并保存父节点和兄弟节点的ID信息 ---
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentSelection.getParent();
                if (parentNode != null) {
                    HistoryNode parentHistoryNode = filterableTree.getUserObject(parentNode);
                    if (parentHistoryNode != null && parentHistoryNode.isNotRoot()) {
                        parentNodeId = parentHistoryNode.getId(); // 保存父节点ID
                    }
                    // 获取兄弟节点列表的ID
                    Enumeration<?> children = parentNode.children();
                    while (children.hasMoreElements()) {
                        DefaultMutableTreeNode sibling = (DefaultMutableTreeNode) children.nextElement();
                        HistoryNode siblingHistoryNode = filterableTree.getUserObject(sibling);
                        if (siblingHistoryNode != null && siblingHistoryNode.isNotRoot()) {
                            siblingNodeIds.add(siblingHistoryNode.getId());
                            // 记录当前选中节点在父节点中的位置
                            if (sibling == currentSelection) {
                                selectedNodeIndexInParent = siblingNodeIds.size() - 1;
                            }
                        }
                    }
                }
            }
        }

        // 记录展开路径的标识信息
        List<String> expandedIds = new ArrayList<>();
        for (TreePath path : expandedPaths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            HistoryNode historyNode = filterableTree.getUserObject(node);
            if (historyNode != null && historyNode.isNotRoot()) {
                expandedIds.add(historyNode.getId());
            }
        }

        // 更新树内容
        filterableTree.update();

        // 如果根节点不显示，就让它显示出来，前提是存在子节点
        DefaultMutableTreeNode root = filterableTree.getRoot();
        if (root.getChildCount() > 0) {
            if (!tree.isRootVisible()) {
                tree.setRootVisible(true);
            }
        } else {
            if (tree.isRootVisible()) {
                tree.setRootVisible(false);
            }
        }

        // 恢复之前节点的展开
        expandMatchingPaths(root, new TreePath(root), expandedIds);

        // 根据操作类型处理选中状态
        TreePath newSelectionPath = null;
        switch (affectType) {
            case ADD_RECORD:
            case ADD_GROUP: {
                // 添加操作：选中新添加的节点
                if (isSameProject) {
                    if (newNodeId != null) {
                        newSelectionPath = findPath(newNodeId);
                    }
                } else {
                    // 选择原先选中的节点
                    if (selectedNodeId != null) {
                        newSelectionPath = findPath(selectedNodeId);
                    }
                }
                break;
            }
            case REMOVE: {
                // 如果是相同项目，执行周边节点的选中
                if (isSameProject) {
                    // 删除操作：优先选择兄弟节点，没有再选择父节点
                    if (selectedNodeId != null) {
                        // 1. 优先尝试选择兄弟节点
                        if (CollUtil.isNotEmpty(siblingNodeIds)) {
                            // 尝试选择下一个兄弟节点
                            if (selectedNodeIndexInParent >= 0 && selectedNodeIndexInParent < siblingNodeIds.size() - 1) {
                                String nextSiblingId = siblingNodeIds.get(selectedNodeIndexInParent + 1);
                                newSelectionPath = findPath(nextSiblingId);
                            }
                            // 如果下一个兄弟不存在或无效，尝试选择上一个兄弟节点
                            if (newSelectionPath == null && selectedNodeIndexInParent > 0) {
                                String prevSiblingId = siblingNodeIds.get(selectedNodeIndexInParent - 1);
                                newSelectionPath = findPath(prevSiblingId);
                            }
                            // 如果索引无效，但兄弟列表不为空，尝试选择第一个兄弟节点
                            if (newSelectionPath == null && !siblingNodeIds.isEmpty()) {
                                String firstSiblingId = siblingNodeIds.get(0);
                                newSelectionPath = findPath(firstSiblingId);
                            }
                        }

                        // 2. 如果没有兄弟节点可选，则尝试选择父节点
                        if (newSelectionPath == null && parentNodeId != null) {
                            newSelectionPath = findPath(parentNodeId);
                        }
                    }
                } else {
                    // 对于不同项目，尝试恢复原先选中的节点
                    if (selectedNodeId != null) {
                        newSelectionPath = findPath(selectedNodeId);
                    }
                    // 如果原节点不存在了（比如恰好也被删除），尝试选择其父节点
                    if (newSelectionPath == null && parentNodeId != null) {
                        newSelectionPath = findPath(parentNodeId);
                    }
                }
                break;
            }
            case UPDATE: {
                // 更新操作：尝试恢复原来的选中节点
                if (selectedNodeId != null) {
                    newSelectionPath = findPath(selectedNodeId);
                }

                break;
            }
            case MOVE: {
                // 移动操作：选中移动后的节点，并确保目标节点展开
                if (selectedNodeId != null) {
                    newSelectionPath = findPath(selectedNodeId);
                }
                break;
            }
        }

        // 设置新的选中路径
        if (newSelectionPath != null) {
            tree.setSelectionPath(newSelectionPath);
            tree.scrollPathToVisible(newSelectionPath);
        } else if (tree.getRowCount() > 0) {
            // 如果没有找到合适的选中路径，但树不为空，则选中根节点
            TreeUtil.ensureSelection(tree);
        }

        if (HistoryAffectType.ADD_RECORD != affectType) requestFocusOnTree(sourceProject);
    }

    private void navigateRecord(String id, boolean needNaming) {
        // 寻找
        DefaultMutableTreeNode node = findNode(id);
        TreePath selectionPath = new TreePath(node.getPath());

        // 选中定位
        tree.setSelectionPath(selectionPath);
        tree.scrollPathToVisible(selectionPath);
        // 判断是否需要重命名
        if (needNaming) {
            HistoryNode historyNode = filterableTree.getUserObject(node);
            // TODO 如果之后加上了全局记录的话，那么这里的项目传递需要从调用源头拿过来
            if (historyNode != null) {
                showRenamePopup(this.project, historyNode);
            }
        }
    }


    /**
     * 递归遍历树，展开所有在expandedIds列表中的节点路径
     */
    private void expandMatchingPaths(DefaultMutableTreeNode node, TreePath path, List<String> expandedIds) {
        HistoryNode historyNode = filterableTree.getUserObject(node);
        if (historyNode != null && historyNode.isNotRoot()) {
            if (expandedIds.contains(historyNode.getId())) {
                tree.expandPath(path); // 只展开之前记录为展开的节点
            }
        }

        // 递归处理子节点
        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            TreePath childPath = path.pathByAddingChild(child);
            expandMatchingPaths(child, childPath, expandedIds);
        }
    }

    public TreePath findPath(String id) {
        DefaultMutableTreeNode node = findNode(id);
        return null == node ? null : new TreePath(node.getPath());
    }

    public DefaultMutableTreeNode findNode(String id) {
        DefaultMutableTreeNode root = filterableTree.getRoot();
        // 使用广度优先枚举
        Enumeration<TreeNode> enumeration = root.breadthFirstEnumeration();

        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            HistoryNode historyNode = (HistoryNode) node.getUserObject();

            // 检查节点是否匹配ID和类型条件
            if (historyNode != null && historyNode.isNotRoot() && id.equals(historyNode.getId())) {
                return node;
            }
        }

        return null;
    }

    private void executeAddGroup() {
        // 在此不需要考虑选中节点的问题，并且直接可以新增到根节点下
        showNewGroupPopup(project, filterableTree.getRootUserObject());
    }

    private void executeAddRecord() {
        NewHistoryRecordAction.newRecord(project, this);
    }

    /**
     * 展示新增组的弹窗
     *
     * @param parentNode 父组节点/根节点
     */
    public void showNewGroupPopup(Project sourceProject, HistoryNode parentNode) {
        NewItemPopupPanel panel = new NewItemPopupPanel(true);
        JTextField textField = panel.getTextField();
        JBPopup popup = NewItemPopupUtil.createNewItemPopup(JsonAssistantBundle.messageOnSystem("popup.new.group.title"), panel, textField);

        panel.setApplyAction(event -> {
            String newName = StrUtil.trim(textField.getText());
            // 新增分组时，名称不能为空
            if (StrUtil.isBlank(newName) || newName.length() > NAME_LENGTH_LIMIT) {
                panel.setError(JsonAssistantBundle.messageOnSystem("error.invalid.name"));
                return;
            }

            String parentId = null;
            JsonGroup parentGroup = parentNode.isGroup() ? (JsonGroup) parentNode.getValue() : null;
            if (null != parentGroup) {
                // 判断组下，是否存在同名分组
                parentId = parentGroup.getId();
                List<JsonGroup> childGroups = historyManager.findChildGroups(parentId);
                if (childGroups.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                    panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                    return;
                }
            } else {
                // 表示是顶层组
                List<JsonGroup> topLevelGroups = historyManager.findTopLevelGroups();
                if (topLevelGroups.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                    panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                    return;
                }
            }

            JsonGroup group = new JsonGroup();
            group.setName(newName);
            group.setParentId(parentId);
            historyManager.addGroup(group);

            // 通过消息机制可以刷新所有项目的树
            ApplicationManager.getApplication().getMessageBus()
                    .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                    .refresh(sourceProject, HistoryAffectType.ADD_GROUP, group.getId());

            popup.closeOk(event);
        });

        popup.showCenteredInCurrentWindow(this.project);
    }

    /**
     * 展示新增记录的弹窗
     *
     * @param sourceProject 源项目
     * @param parentNode    父组节点/根节点
     */
    public void showNewRecordPopup(Project sourceProject, HistoryNode parentNode) {
        // 第一个做文本展示，第二个做图标展示，第三个做标识符
        CreateWithTemplatesDialogPanel.TemplatePresentation jsonTemp =
                new CreateWithTemplatesDialogPanel.TemplatePresentation(DataFormatType.JSON.getValue(), JsonAssistantIcons.FileTypes.JSON_NODE, DataFormatType.JSON.getValue());
        CreateWithTemplatesDialogPanel.TemplatePresentation json5Temp =
                new CreateWithTemplatesDialogPanel.TemplatePresentation(DataFormatType.JSON5.getValue(), JsonAssistantIcons.FileTypes.JSON5_NODE, DataFormatType.JSON5.getValue());

        List<CreateWithTemplatesDialogPanel.TemplatePresentation> myTemplatesList = ListUtil.list(false, jsonTemp, json5Temp);

        CreateWithTemplatesDialogPanel contentPanel = new CreateWithTemplatesDialogPanel(DataFormatType.JSON.getValue(), myTemplatesList);
        JTextField textField = contentPanel.getNameField();
        JBPopup popup = NewItemPopupUtil.createNewItemPopup(JsonAssistantBundle.messageOnSystem("popup.new.record.title"), contentPanel, textField);

        contentPanel.setApplyAction(event -> {
            String newName = StrUtil.trim(textField.getText());
            // 新增记录时，名称不能为空
            if (StrUtil.isBlank(newName) || newName.length() > NAME_LENGTH_LIMIT) {
                contentPanel.setError(JsonAssistantBundle.messageOnSystem("error.invalid.name"));
                return;
            }

            String parentId = null;
            JsonGroup parentGroup = parentNode.isGroup() ? (JsonGroup) parentNode.getValue() : null;
            if (null != parentGroup) {
                parentId = parentGroup.getId();
                // 判断组下，是否存在同名记录
                List<JsonRecord> records = historyManager.findRecords(parentGroup.getRecordIds());
                if (records.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                    contentPanel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                    return;
                }

            } else {
                // 表示是孤立记录
                List<JsonRecord> topLevelNotes = historyManager.findTopLevelRecords();
                if (topLevelNotes.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                    contentPanel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                    return;
                }
            }

            String fileExtension = DataFormatType.JSON.getValue().equals(contentPanel.getSelectedTemplate())
                    ? FileTypes.JSON.getExtension()
                    : FileTypes.JSON5.getExtension();

            // 添加记录
            JsonRecord record = new JsonRecord();
            record.setId(IdUtil.simpleUUID());
            record.setName(newName);
            record.setFileExtension(fileExtension);
            record.setParentId(parentId);
            historyManager.addRecord(record, true);

            // 在此方法选中新增记录后，监听器会创建新的编辑器
            // 通过消息机制可以刷新所有项目的树
            ApplicationManager.getApplication().getMessageBus()
                    .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                    .refresh(sourceProject, HistoryAffectType.ADD_RECORD, record.getId());

            requestFocusOnEditor(sourceProject);
            popup.closeOk(event);
        });

        popup.showCenteredInCurrentWindow(this.project);
    }

    /**
     * 展示目标组选择的弹窗
     *
     * @param groupNodes 可选择的组节点
     */
    public void showTargetGroupSelectionDialog(Project sourceProject, List<HistoryNode> groupNodes, boolean isAddGroup) {
        TargetGroupSelectionDialog dialog = new TargetGroupSelectionDialog(sourceProject, groupNodes);
        if (dialog.showAndGet()) {
            HistoryNode selectedNode = dialog.getSelectedNode();
            if (isAddGroup) showNewGroupPopup(sourceProject, selectedNode);
            else showNewRecordPopup(sourceProject, selectedNode);
        }
    }

    public void showRenamePopup(Project sourceProject, HistoryNode currentNote) {
        NewItemPopupPanel panel = new NewItemPopupPanel(true);
        JTextField textField = panel.getTextField();
        // 如果只有短名，不存在名称，那就显示为空
        String nodeName = currentNote.getNodeName();
        if (currentNote.isRecord()) {
            JsonRecord record = (JsonRecord) currentNote.getValue();
            String name = record.getName();
            nodeName = StrUtil.isBlank(name) ? StrUtil.EMPTY : name;
        }

        textField.setText(nodeName);
        textField.selectAll();

        JBPopup popup = NewItemPopupUtil.createNewItemPopup(JsonAssistantBundle.messageOnSystem("popup.rename.title"), panel, textField);

        panel.setApplyAction(event -> {
            String newName = textField.getText();
            if (StrUtil.isBlank(newName) || newName.length() > NAME_LENGTH_LIMIT) {
                panel.setError(JsonAssistantBundle.messageOnSystem("error.invalid.name"));
                return;
            }

            boolean isGroup = currentNote.isGroup();
            HistoryNode parentNode = currentNote.getParentNode();
            JsonGroup parentGroup = parentNode.isRoot() ? null : (JsonGroup) parentNode.getValue();

            // 区分记录和分组
            if (null != parentGroup) {
                // 查找自己的父分组，查看是否有同名
                if (isGroup) {
                    List<JsonGroup> childGroups = historyManager.findChildGroups(parentGroup.getId());
                    if (childGroups.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                        panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                        return;
                    }
                } else {
                    List<JsonRecord> records = historyManager.findRecords(parentGroup.getRecordIds());
                    if (records.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                        panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                        return;
                    }
                }

            } else {
                // 查找顶层分组或顶层记录，查看是否有同名
                if (isGroup) {
                    List<JsonGroup> topLevelGroups = historyManager.findTopLevelGroups();
                    if (topLevelGroups.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                        panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                        return;
                    }

                } else {
                    List<JsonRecord> topLevelNotes = historyManager.findTopLevelRecords();
                    if (topLevelNotes.stream().map(BaseData::getName).collect(Collectors.toList()).contains(newName)) {
                        panel.setError(JsonAssistantBundle.messageOnSystem("error.duplicate.name"));
                        return;
                    }
                }
            }

            // 改名的话不需要更新更新时间
            currentNote.getValue().setName(newName);
            // 直接保存
            historyManager.saveState();

            // 通过消息机制可以刷新所有项目的树
            ApplicationManager.getApplication().getMessageBus()
                    .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                    .refresh(sourceProject, HistoryAffectType.UPDATE, null);

            popup.closeOk(event);
        });

        popup.showCenteredInCurrentWindow(project);
    }

    public void requestFocusOnTree(Project sourceProject) {
        if (Objects.equals(this.project, sourceProject)) {
            IdeFocusManager.findInstance().requestFocus(tree, true);
        }
    }

    public void requestFocusOnEditor(Project sourceProject) {
        if (Objects.equals(this.project, sourceProject)) {
            IdeFocusManager.findInstance().requestFocus(activeEditor.getContentComponent(), true);
        }
    }

    /**
     * 立即释放指定记录的编辑器
     *
     * @param nodes 指定记录
     */
    public synchronized void releaseEditorImmediately(List<HistoryNode> nodes) {
        Runnable task = () -> {
            for (HistoryNode node : nodes) {
                // 从编辑器池中移除记录对应的编辑器
                Editor editor = editorPool.remove(node);
                // 编辑器已不存在，直接返回
                if (editor == null) continue;
                // 取消该记录可能已存在的旧定时任务，避免重复安排任务
                cancelPendingCloseForRecord(node);
                // 立即关闭
                if (!editor.isDisposed()) {
                    ApplicationManager.getApplication().invokeLater(() -> editorFactory.releaseEditor(editor));
                }
            }
        };

        Application application = ApplicationManager.getApplication();
        // 验证是否为事件线程
        if (application.isDispatchThread()) {
            task.run();
        } else {
            application.invokeLater(task);
        }
    }

    /**
     * 立即释放所有编辑器
     */
    public synchronized void releaseAllEditorImmediately() {
        Runnable task = () -> {
            // 清除所有任务进程
            centralAlarm.cancelAllRequests();
            // 清除任务记录
            scheduledTasks.clear();

            for (Editor editor : editorPool.values()) {
                if (!editor.isDisposed()) {
                    ApplicationManager.getApplication().invokeLater(() -> editorFactory.releaseEditor(editor));
                }
            }

            editorPool.clear();
        };

        Application application = ApplicationManager.getApplication();
        // 验证是否为事件线程
        if (application.isDispatchThread()) {
            task.run();
        } else {
            application.invokeLater(task);
        }
    }

    /**
     * 移除记录（不包括源文件）
     */
    private void removeRecordNotification(AnActionEvent event, JsonRecord record) {
        historyManager.delRecord(record);
        // 通过消息机制可以刷新所有项目的树
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(RefreshHistoryTreeEvent.TOPIC)
                .refresh(event.getProject(), HistoryAffectType.REMOVE, null);
    }

    /**
     * 创建记录源文件，与记录相关联
     */
    private void newRecordFileNotification(JsonRecord record, HistoryNode selectedNode) {
        VirtualFile sourceFile = historyManager.createSourceFile(record.getId(), record.getFileExtension(), null);
        record.setSourceFile(sourceFile);
        // 判断当前记录是否为此记录，若是，则需要自己刷新组件
        if (Objects.equals(selectedNode, currentRecord)) {
            setupEditorContent(selectedNode, sourceFile);

        } else {
            // 重新选中该记录，以触发 showNoteContent 方法
            TreeUtil.selectNode(tree, findNode(record.getId()));
        }

        // 刷新树，把警告图标换成正常的
        UIUtils.repaintComponent(tree);
    }

    public void requestFocusOnStructureComponent() {
        IdeFocusManager.findInstance().requestFocus(tree, true);
    }


    public @Nullable JComponent getPreferredFocusedComponent() {
        return tree;
    }

    public Tree getTree() {
        return tree;
    }

    public Project getProject() {
        return project;
    }

    public @Nullable Editor getActiveEditor() {
        return activeEditor;
    }

    public @Nullable VirtualFile getCurrentFile() {
        return null != activeEditor ? activeEditor.getUserData(FILE_KEY) : null;
    }

    public HistoryNode getCurrentRecord() {
        return currentRecord;
    }

    /**
     * 获取暂时未启用的所有编辑器
     *
     * @return 编辑器列表
     */
    public List<Editor> getRestEditors() {
        List<Editor> editors = new ArrayList<>(editorPool.values());
        editors.remove(activeEditor);
        return editors;
    }

    public HistoryNode getRootHistoryNode() {
        return filterableTree.getRootUserObject();
    }

    public DefaultMutableTreeNode getRootNode() {
        return filterableTree.getRoot();
    }

    @Override
    public void dispose() {
        // 释放编辑器
        activeEditor = null;

        centralAlarm.cancelAllRequests();
        scheduledTasks.clear();
        editorPool.values().forEach(editorFactory::releaseEditor);
        editorPool.clear();
    }

}
