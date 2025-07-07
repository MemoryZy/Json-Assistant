package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.history.AddHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.EditHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.RemoveHistoryAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.enums.HistoryDisplayMode;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.event.HistoryAddedEvent;
import cn.memoryzy.json.event.HistoryViewChangedEvent;
import cn.memoryzy.json.event.NavigateRecordEvent;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.HistoryState;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.listener.history.AddAction;
import cn.memoryzy.json.ui.listener.history.CancelAction;
import cn.memoryzy.json.ui.listener.history.UpdateAction;
import cn.memoryzy.json.ui.node.HistoryTreeNode;
import cn.memoryzy.json.ui.panel.AutoCompleteWrapper;
import cn.memoryzy.json.ui.panel.EditWrapper;
import cn.memoryzy.json.util.*;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/6/26
 */
public class HistoryToolWindowComponentProvider implements Disposable {

    /**
     * 分割比例持久化
     */
    public static final String SPLITTER_PROPORTION_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".HistorySplitterProportionKey";

    public static final Key<String> HISTORY_EDITOR_FLAG = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".HISTORY_EDITOR_FLAG");

    private final Project project;
    private final HistoryManager historyManager;
    private final HistoryState historyState;

    /**
     * 消息总线（项目级）
     */
    private final MessageBusConnection projectConnection;

    private final SimpleToolWindowPanel windowPanel;

    // ----------------------- left
    private final AutoCompleteWrapper completeWrapper;
    private final JBCardLayout cardLayout;
    private final JPanel cardPanel;
    private final JBList<JsonRecord> showList;
    private final Tree showTree;

    // ----------------------- right
    private final Editor recordEditor;
    private final JPanel updatePanel;
    private final EditWrapper nameEditorWrapper;
    private final JButton addButton;
    private final JButton updateButton;
    private final JButton cancelButton;

    /**
     * 刷新编辑器光标
     */
    private int lastLineCount = 0;

    public HistoryToolWindowComponentProvider(Project project) {
        this.project = project;
        this.historyManager = HistoryManager.getInstance(project);
        this.historyState = ToolWindowSettings.getInstance().getHistoryState();
        this.projectConnection = project.getMessageBus().connect(historyManager);

        this.windowPanel = new SimpleToolWindowPanel(false, false);
        // ----------------------- left
        this.cardLayout = new JBCardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.showList = new JBList<>(createListModel());
        this.showTree = new Tree(createTreeModel());
        this.completeWrapper = new AutoCompleteWrapper(project, getLatestVariants(), () -> PluginConstant.HISTORY_SEARCH_HISTORY_KEY);

        // ----------------------- right
        this.recordEditor = createJsonEditor();
        this.nameEditorWrapper = new EditWrapper(project, JsonAssistantBundle.messageOnSystem("toolwindow.history.edit.action.name"));
        this.addButton = new JButton(new AddAction(this::addRecord));
        this.updateButton = new JButton(new UpdateAction(this::updateRecord));
        this.cancelButton = new JButton(new CancelAction(this::dismissEditView));
        this.updatePanel = new JPanel(new GridBagLayout());
    }

    public JComponent createComponent() {
        JBSplitter splitter = new JBSplitter(false, 0.3f);
        splitter.setAndLoadSplitterProportionKey(SPLITTER_PROPORTION_KEY);
        splitter.setFirstComponent(createFirstComponent());
        splitter.setSecondComponent(createSecondComponent());

        // 注册配置更新事件
        registerConfigurationUpdateEventHandlers();
        // 注册历史记录添加事件
        registerHistoryAddedEventHandlers();
        // 注册精确找到记录事件
        registerNavigateRecordEventHandlers();

        windowPanel.setToolbar(createToolbar(windowPanel));
        windowPanel.setContent(splitter);
        return windowPanel;
    }

    private JComponent createFirstComponent() {
        // CardLayout 切换 Tree 和 JBList 展示
        JScrollPane listScrollPane = createListScrollPane();
        JScrollPane treeScrollPane = createTreeScrollPane();

        cardPanel.add(listScrollPane, HistoryDisplayMode.LIST.name());
        cardPanel.add(treeScrollPane, HistoryDisplayMode.TREE.name());

        // 默认显示
        cardLayout.show(cardPanel, historyState.getHistoryDisplayMode().name());

        // 注册文档监听
        completeWrapper.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                // 对比时需要不区分大小写
                String filterName = StrUtil.trim(event.getDocument().getText().toLowerCase());

                filterListItem(filterName);
                filterTreeNode(filterName);
            }
        });

        // 添加键盘事件监听
        completeWrapper.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP) {
                    handleNavigationKey();
                }
            }
        });

        return new BorderLayoutPanel().addToTop(completeWrapper).addToCenter(cardPanel);
    }

    private JComponent createToolbar(SimpleToolWindowPanel windowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new AddHistoryAction(this, windowPanel));
        actionGroup.add(new RemoveHistoryAction(this, windowPanel));
        actionGroup.add(new EditHistoryAction(this, windowPanel));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true);
        toolbar.setTargetComponent(windowPanel);
        return toolbar.getComponent();
    }


    // 处理上下键导航
    private void handleNavigationKey() {
        JComponent component = HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode() ? showList : showTree;
        IdeFocusManager.findInstance().requestFocus(component, true);
    }


    // 查找第一个可见节点（树）
    private TreePath findFirstVisiblePath() {
        HistoryTreeNode root = (HistoryTreeNode) showTree.getModel().getRoot();

        // 找到第一个可见的组节点
        TreeNode firstGroup = root.getChildAt(0);
        if (firstGroup.getChildCount() > 0) {
            // 组节点下有子节点，选择第一个子节点
            return new TreePath(new Object[]{root, firstGroup, firstGroup.getChildAt(0)});
        } else {
            // 没有子节点，选择组节点本身
            return new TreePath(new Object[]{root, firstGroup});
        }
    }

    private JComponent createSecondComponent() {
        configureUpdatePanel();

        return new BorderLayoutPanel()
                .addToCenter(recordEditor.getComponent())
                .addToBottom(updatePanel);
    }

    private void configureUpdatePanel() {
        nameEditorWrapper.setPlaceholder("Name");
        nameEditorWrapper.setShowPlaceholderWhenFocused(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(3); // 组件间距

        // 1. 左侧输入框（占据剩余空间）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // 关键：水平权重
        updatePanel.add(nameEditorWrapper, gbc);

        // 2. 右侧按钮组（留出右侧空间）
        gbc.gridx = 1;
        gbc.weightx = 0; // 重置权重
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.TRAILING, 3, 0));
        buttonGroup.add(addButton);
        buttonGroup.add(updateButton);
        buttonGroup.add(cancelButton);
        buttonGroup.setBorder(JBUI.Borders.emptyRight(8)); // 右侧15px空隙
        updatePanel.add(buttonGroup, gbc);

        updatePanel.setBorder(JBUI.Borders.empty(5, 0, 3, 0));

        // 默认隐藏
        updatePanel.setVisible(false);
        addButton.setVisible(false);
        updateButton.setVisible(false);
    }

    private JScrollPane createListScrollPane() {
        showList.setFont(UIUtils.jetBrainsMonoFont(JBUIScale.scaleFontSize(13)));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.setCellRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends JsonRecord> list, JsonRecord value, int index, boolean selected, boolean hasFocus) {
                String name = value.getName();
                append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
                append((StrUtil.isNotBlank(name) ? name : value.getDisplayText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                // setIcon(AllIcons.FileTypes.Json);

                if (!list.isEnabled()) {
                    setEnabled(false);
                    list.setForeground(JBColor.GRAY);
                    list.setToolTipText(JsonAssistantBundle.messageOnSystem("tooltip.history.tree.disabled.text"));
                } else {
                    if (!isEnabled()) setEnabled(true);
                    list.setForeground(UIUtil.getListForeground());
                    list.setToolTipText(null);
                }

                SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
            }
        });
        showList.addListSelectionListener(e -> {
            if (HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode()) {
                refreshEditor(showList.getSelectedValue());
            }
        });
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));

        // 将标准方向键（↑↓←→）、PageUp/PageDown、Home/End 等按键绑定到列表的滚动操作
        ScrollingUtil.installActions(showList);
        // 确保列表始终存在有效的选中项
        ScrollingUtil.ensureSelectionExists(showList);
        // 滚动包装
        return UIUtils.wrapScrollPane(showList);
    }

    private JScrollPane createTreeScrollPane() {
        showTree.setDragEnabled(true);
        showTree.setExpandableItemsEnabled(true);
        showTree.setRootVisible(false);
        showTree.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
        showTree.setFont(UIUtils.jetBrainsMonoFont(JBUIScale.scaleFontSize(13)));
        showTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        showTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                HistoryTreeNode treeNode = (HistoryTreeNode) value;
                HistoryTreeNodeType nodeType = treeNode.getNodeType();

                if (HistoryTreeNodeType.NODE.equals(nodeType)) {
                    setIcon(AllIcons.FileTypes.Json);
                    append(treeNode.toString());
                } else {
                    setIcon(JsonAssistantIcons.GROUP);
                    append(treeNode + " (" + treeNode.getSize() + ")");
                }

                if (!tree.isEnabled()) {
                    setEnabled(false);
                    tree.setForeground(JBColor.GRAY);
                    tree.setToolTipText(JsonAssistantBundle.messageOnSystem("tooltip.history.tree.disabled.text"));
                } else {
                    if (!isEnabled()) setEnabled(true);
                    tree.setForeground(UIUtil.getTreeForeground());
                    tree.setToolTipText(null);
                }

                SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
            }
        });

        showTree.addTreeSelectionListener(e -> {
            if (HistoryDisplayMode.TREE == historyState.getHistoryDisplayMode()) {
                TreePath selectionPath = showTree.getSelectionPath();
                if (selectionPath != null) {
                    HistoryTreeNode treeNode = (HistoryTreeNode) selectionPath.getLastPathComponent();
                    if (HistoryTreeNodeType.GROUP.equals(treeNode.getNodeType())) {
                        clearEditor();
                    } else {
                        refreshEditor(treeNode.getValue());
                    }
                }
            }
        });

        expandSingleNode();

        return UIUtils.wrapScrollPane(showTree);
    }

    public JComponent getPreferredFocusedComponent() {
        return HistoryDisplayMode.TREE == historyState.getHistoryDisplayMode() ? showTree : showList;
    }

    private void filterTreeNode(String filterName) {
        DefaultTreeModel model = (DefaultTreeModel) showTree.getModel();
        HistoryTreeNode rootNode = (HistoryTreeNode) model.getRoot();

        // 清空过滤：恢复完整树结构
        if (StrUtil.isBlank(filterName)) {
            model.setRoot(combineRootTreeNode());
            expandSingleNode(); // 恢复默认展开状态
            return;
        }

        // 创建新根节点
        HistoryTreeNode newRoot = new HistoryTreeNode();
        Enumeration<TreeNode> groups = rootNode.children();

        while (groups.hasMoreElements()) {
            HistoryTreeNode groupNode = (HistoryTreeNode) groups.nextElement();
            HistoryTreeNode filteredGroup = new HistoryTreeNode(null, groupNode.toString(), 0, HistoryTreeNodeType.GROUP);
            Enumeration<TreeNode> records = groupNode.children();

            while (records.hasMoreElements()) {
                HistoryTreeNode recordNode = (HistoryTreeNode) records.nextElement();
                JsonRecord record = recordNode.getValue();

                // 匹配逻辑：名称或原始内容
                boolean matches = (record.getName() != null && record.getName().toLowerCase().contains(filterName)) ||
                        (record.getRawText() != null && record.getRawText().toLowerCase().contains(filterName));

                if (matches) {
                    // 复制匹配的节点
                    HistoryTreeNode cloned = new HistoryTreeNode(record, null, null, HistoryTreeNodeType.NODE);
                    filteredGroup.add(cloned);
                    filteredGroup.setSize(filteredGroup.getSize() + 1); // 更新组大小
                }
            }

            // 添加非空组
            if (filteredGroup.getSize() > 0) {
                newRoot.add(filteredGroup);
            }
        }

        // 更新树模型并展开所有
        model.setRoot(newRoot);
        expandAllGroups();
    }

    private void filterListItem(String filterName) {
        // 获取原始数据模型
        DefaultListModel<JsonRecord> model = (DefaultListModel<JsonRecord>) showList.getModel();
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();

        if (StrUtil.isBlank(filterName)) {
            // 清空过滤：恢复完整列表
            model.removeAllElements();
            model.addAll(recentHistories);
            // 确保选中项存在
            ScrollingUtil.ensureSelectionExists(showList);
            return;
        }

        // 过滤逻辑：名称或原始内容匹配
        List<JsonRecord> filtered = recentHistories.stream()
                .filter(record -> {
                    String name = record.getName();
                    String rawText = record.getRawText();
                    return (name != null && name.toLowerCase().contains(filterName)) ||
                            (rawText != null && rawText.toLowerCase().contains(filterName));
                })
                .collect(Collectors.toList());

        // 更新列表模型
        model.removeAllElements();
        model.addAll(filtered);

        // 处理选中项
        if (!model.isEmpty()) {
            showList.setSelectedIndex(0); // 自动选中第一项
        }
    }

    private DefaultListModel<JsonRecord> createListModel() {
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();
        return JBList.createDefaultListModel(recentHistories);
    }

    private TreeModel createTreeModel() {
        return new DefaultTreeModel(combineRootTreeNode());
    }

    private TreeNode combineRootTreeNode() {
        HistoryTreeNode rootNode = new HistoryTreeNode();
        Map<String, List<JsonRecord>> group = historyManager.groupByUpdateTime();

        // 将时间进行排序
        List<Map.Entry<String, List<JsonRecord>>> recordList = group.entrySet().stream()
                .sorted(Comparator.comparing(
                        el -> PluginConstant.UNKNOWN.equals(el.getKey())
                                ? LocalDate.MIN
                                : LocalDate.parse(el.getKey(), DatePattern.NORM_DATE_FORMATTER)))
                .collect(Collectors.toList());

        // 反转
        Collections.reverse(recordList);

        for (Map.Entry<String, List<JsonRecord>> entry : recordList) {
            String key = entry.getKey();
            List<JsonRecord> value = entry.getValue();

            // 排序List
            value.sort(Comparator.comparing(JsonRecord::getUpdateTime).reversed());

            // Map第一层是组节点
            HistoryTreeNode groupNode = new HistoryTreeNode(null, key, value.size(), HistoryTreeNodeType.GROUP);

            // 添加底层数据节点
            for (JsonRecord record : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryTreeNode(record, null, null, HistoryTreeNodeType.NODE));
            }

            rootNode.add(groupNode);
        }

        return rootNode;
    }

    private Editor createJsonEditor() {
        EditorEx editor = (EditorEx) PlatformUtil.createEditor(project, "record", FileTypeHolder.JSON5, true, EditorKind.MAIN_EDITOR, "");
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

        // 标记
        editor.putUserData(HISTORY_EDITOR_FLAG, JsonAssistantPlugin.PLUGIN_AUTHOR);
        return editor;
    }

    private Collection<String> getLatestVariants() {
        Collection<String> variants = new ArrayList<>();
        DefaultListModel<JsonRecord> model = (DefaultListModel<JsonRecord>) showList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            JsonRecord record = model.elementAt(i);
            String recordName = record.getName();
            if (StrUtil.isNotBlank(recordName)) {
                variants.add(recordName);
            }
        }

        return variants;
    }


    private void registerConfigurationUpdateEventHandlers() {
        JsonAssistantToolWindowComponentProvider.APPLICATION_CONNECTION.subscribe(HistoryViewChangedEvent.TOPIC, (HistoryViewChangedEvent) this::applyViewMode);
    }

    private void registerHistoryAddedEventHandlers() {
        projectConnection.subscribe(HistoryAddedEvent.TOPIC, (HistoryAddedEvent) this::refreshHistoryComponent);
    }

    private void registerNavigateRecordEventHandlers() {
        projectConnection.subscribe(NavigateRecordEvent.TOPIC, (NavigateRecordEvent) this::navigateRecord);
    }

    private void applyViewMode(HistoryDisplayMode mode) {
        cardLayout.show(cardPanel, mode.name());
    }

    private void refreshHistoryComponent() {
        refreshListComponent();
        refreshTreeComponent();
        completeWrapper.setText(null);

        // 确保有选中项
        if (historyState.getHistoryDisplayMode() == HistoryDisplayMode.LIST) {
            if (showList.getModel().getSize() > 0 && showList.getSelectedIndex() == -1) {
                showList.setSelectedIndex(0);
            }
        } else {
            if (showTree.getSelectionCount() == 0) {
                TreePath firstPath = findFirstVisiblePath();
                showTree.setSelectionPath(firstPath);
            }
        }
    }

    private void navigateRecord(Integer recordId, boolean shouldEdit) {
        // 请退出编辑模式后再进行查看
        if (!showList.isEnabled()) {
            dismissEditView();
        }

        // 选中对应记录id的记录
        if (HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode()) {
            navigateInList(recordId, shouldEdit);
        } else {
            navigateInTree(recordId, shouldEdit);
        }
    }

    private void navigateInList(Integer recordId, boolean shouldEdit) {
        DefaultListModel<JsonRecord> model = (DefaultListModel<JsonRecord>) showList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            JsonRecord record = model.getElementAt(i);
            if (record.getId().equals(recordId)) {
                // 选中记录
                showList.setSelectedIndex(i);
                showList.scrollRectToVisible(showList.getCellBounds(i, i));

                // 如果需要编辑
                if (shouldEdit) {
                    executeEditAction();
                    // 名称编辑器获得焦点
                    IdeFocusManager.findInstance().requestFocus(nameEditorWrapper.getPreferredFocusedComponent(), true);
                }

                break;
            }
        }
    }

    private void navigateInTree(Integer recordId, boolean shouldEdit) {
        DefaultTreeModel model = (DefaultTreeModel) showTree.getModel();
        HistoryTreeNode root = (HistoryTreeNode) model.getRoot();

        // 遍历树查找记录
        TreePath foundPath = findRecordPath(root, recordId);

        if (foundPath != null) {
            // 确保路径可见并选中
            showTree.expandPath(foundPath.getParentPath());
            showTree.setSelectionPath(foundPath);
            showTree.scrollPathToVisible(foundPath);

            // 如果需要编辑
            if (shouldEdit) {
                executeEditAction();
                // 名称编辑器获得焦点
                IdeFocusManager.findInstance().requestFocus(nameEditorWrapper.getPreferredFocusedComponent(), true);
            }
        } else {
            // 未找到记录
            clearEditor();
        }
    }

    private TreePath findRecordPath(TreeNode root, Integer recordId) {
        Enumeration<?> groups = root.children();

        while (groups.hasMoreElements()) {
            HistoryTreeNode group = (HistoryTreeNode) groups.nextElement();

            // 检查组内的所有记录
            Enumeration<?> records = group.children();
            while (records.hasMoreElements()) {
                HistoryTreeNode recordNode = (HistoryTreeNode) records.nextElement();
                if (recordNode.getValue().getId().equals(recordId)) {
                    return new TreePath(recordNode.getPath());
                }
            }
        }

        return null;
    }

    private void refreshListComponent() {
        JsonRecord selectedValue = showList.getSelectedValue();
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();

        DefaultListModel<JsonRecord> model = (DefaultListModel<JsonRecord>) showList.getModel();
        model.removeAllElements();
        model.addAll(recentHistories);

        if (null != selectedValue && CollUtil.isNotEmpty(recentHistories)) {
            // 依旧选中刚才选择的元素
            showList.setSelectedIndex(recentHistories.indexOf(selectedValue));
        } else {
            // 确保列表始终存在有效的选中项
            ScrollingUtil.ensureSelectionExists(showList);
        }
    }

    private void refreshTreeComponent() {
        DefaultTreeModel model = (DefaultTreeModel) showTree.getModel();
        // 记录树节点展开状态
        Map<TreePath, Boolean> expandedStates = UIUtils.recordExpandedStates(showTree);

        // 重构前保存节点信息
        SavedNodeInfo savedInfo = saveCurrentSelectionInfo();

        // 重新构建节点
        TreeNode newRootNode = combineRootTreeNode();
        model.setRoot(newRootNode);

        // 恢复展开状态
        restoreExpandedStates(expandedStates, newRootNode);

        // 重新选中
        restoreSelection(newRootNode, savedInfo);
    }

    private SavedNodeInfo saveCurrentSelectionInfo() {
        SavedNodeInfo savedInfo = new SavedNodeInfo();
        TreePath selectionPath = showTree.getSelectionPath();

        if (selectionPath != null) {
            savedInfo.isSelected = true;
            HistoryTreeNode selectedNode = (HistoryTreeNode) selectionPath.getLastPathComponent();
            if (null != selectedNode) {
                TreeNode parentNode = selectedNode.getParent();
                if (HistoryTreeNodeType.GROUP == selectedNode.getNodeType()) {
                    // 组节点（父节点是根）
                    savedInfo.isGroup = true;
                    savedInfo.groupDate = selectedNode.toString();
                    savedInfo.groupIndex = parentNode.getIndex(selectedNode);
                } else {
                    // 记录节点
                    savedInfo.isGroup = false;
                    savedInfo.recordId = selectedNode.getValue().getId();
                    savedInfo.recordIndex = parentNode.getIndex(selectedNode);
                    savedInfo.groupDate = parentNode.toString(); // 父组节点的日期
                }
            }
        }

        return savedInfo;
    }

    private void restoreSelection(TreeNode root, SavedNodeInfo savedInfo) {
        if (!savedInfo.isSelected) return;

        TreeNode targetNode = null;
        if (savedInfo.isGroup) {
            // 尝试恢复组节点
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
                if (groupNode.toString().equals(savedInfo.groupDate)) {
                    targetNode = groupNode;
                    break;
                }
            }

            if (targetNode != null) {
                // 直接选中组节点
                UIUtils.selectNode(showTree, targetNode);
            } else {
                // 组节点被删除：检查剩余组节点数量
                int groupCount = root.getChildCount();
                if (groupCount == 1) {
                    DefaultMutableTreeNode remainingGroup = (DefaultMutableTreeNode) root.getChildAt(0);
                    showTree.expandPath(new TreePath(remainingGroup.getPath()));
                    if (remainingGroup.getChildCount() > 0) {
                        // 选第一条记录
                        UIUtils.selectNode(showTree, remainingGroup.getChildAt(0));
                    } else {
                        UIUtils.selectNode(showTree, remainingGroup); // 没有记录则选组节点
                    }
                } else {
                    // 选中第一个组节点（或其他逻辑）
                    UIUtils.selectNode(showTree, root.getChildAt(0));
                }
            }
        } else {
            // 尝试恢复记录节点
            DefaultMutableTreeNode targetGroup = null;
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
                if (groupNode.toString().equals(savedInfo.groupDate)) {
                    targetGroup = groupNode;
                    break;
                }
            }

            if (targetGroup != null) {
                // 在当前组内查找记录
                for (int i = 0; i < targetGroup.getChildCount(); i++) {
                    HistoryTreeNode recordNode = (HistoryTreeNode) targetGroup.getChildAt(i);
                    JsonRecord record = recordNode.getValue();

                    if (record.getId().equals(savedInfo.recordId)) {
                        targetNode = recordNode;
                        break;
                    }
                }

                if (targetNode != null) {
                    UIUtils.selectNode(showTree, targetNode);
                } else {
                    // 记录被删除：尝试选后一个兄弟节点
                    int newIndex = savedInfo.recordIndex < targetGroup.getChildCount()
                            ? savedInfo.recordIndex
                            : targetGroup.getChildCount() - 1;

                    if (newIndex >= 0) {
                        UIUtils.selectNode(showTree, targetGroup.getChildAt(newIndex));
                    } else {
                        // 没有子节点则选中组节点
                        UIUtils.selectNode(showTree, targetGroup);
                    }
                }
            } else {
                // 组节点也被删除，回退到组节点的处理逻辑
                int groupCount = root.getChildCount();
                if (groupCount == 1) {
                    DefaultMutableTreeNode remainingGroup = (DefaultMutableTreeNode) root.getChildAt(0);
                    showTree.expandPath(new TreePath(remainingGroup.getPath()));
                    if (remainingGroup.getChildCount() > 0) {
                        UIUtils.selectNode(showTree, remainingGroup.getChildAt(0));
                    } else {
                        UIUtils.selectNode(showTree, remainingGroup);
                    }
                } else if (groupCount > 0) {
                    UIUtils.selectNode(showTree, root.getChildAt(0)); // 选第一个组节点
                }
            }
        }
    }


    /**
     * 根据名称展开节点
     *
     * @param expandedStates 展开节点记录
     * @param newRootNode    新的Root节点
     */
    private void restoreExpandedStates(Map<TreePath, Boolean> expandedStates, TreeNode newRootNode) {
        // 恢复树节点展开状态（这里根据名称来实现展开）
        List<? extends TreeNode> childList = JsonAssistantUtil.enumerationToList(newRootNode.children());

        // 遍历之前记录的展开节点
        for (Map.Entry<TreePath, Boolean> entry : expandedStates.entrySet()) {
            if (entry.getValue()) {
                TreePath path = null;
                TreeNode keyNode = (TreeNode) entry.getKey().getLastPathComponent();
                // 获取展开节点的展示名称，根节点为null
                String groupName = keyNode.toString();
                // 跳过根节点
                if (groupName == null) continue;

                // 遍历二级节点
                for (TreeNode treeNode : childList) {
                    // 二级节点展示名称
                    String nodeString = treeNode.toString();
                    // 匹配名称
                    if (Objects.equals(groupName, nodeString)) {
                        path = new TreePath(((HistoryTreeNode) treeNode).getPath());
                        break;
                    }
                }

                // 实现展开
                if (path != null) {
                    showTree.expandPath(path);
                }
            }
        }
    }


    // 辅助方法：展开所有组节点
    private void expandAllGroups() {
        HistoryTreeNode root = (HistoryTreeNode) showTree.getModel().getRoot();
        Enumeration<TreeNode> groups = root.children();

        while (groups.hasMoreElements()) {
            TreeNode group = groups.nextElement();
            TreePath path = new TreePath(((DefaultMutableTreeNode) group).getPath());
            showTree.expandPath(path);
        }

        // 自动选中第一个节点
        if (root.getChildCount() > 0) {
            TreeNode firstGroup = root.getChildAt(0);
            if (firstGroup.getChildCount() > 0) {
                UIUtils.selectNode(showTree, firstGroup.getChildAt(0));
            } else {
                UIUtils.selectNode(showTree, firstGroup);
            }
        }
    }

    private void expandSingleNode() {
        HistoryTreeNode rootNode = (HistoryTreeNode) showTree.getModel().getRoot();
        List<TreeNode> children = JsonAssistantUtil.enumerationToList(rootNode.children());
        // 若只有一个节点
        if (children.size() == 1) {
            HistoryTreeNode node = (HistoryTreeNode) children.get(0);
            // 展开
            showTree.expandPath(new TreePath(node.getPath()));
            // 选中该节点下的第一个元素
            List<TreeNode> nodeList = JsonAssistantUtil.enumerationToList(node.children());
            // 第一个节点元素
            HistoryTreeNode child = (HistoryTreeNode) nodeList.get(0);
            // 转为树路径
            TreePath path = new TreePath(child.getPath());
            // 选中节点
            showTree.setSelectionPath(path);
        }
    }


    public void executeEditAction() {
        // 获取当前选中的元素
        JsonRecord record = getCurrentSelectionValue();
        // 名称
        nameEditorWrapper.setText(record.getName());
        ((UpdateAction) updateButton.getAction()).setRecord(record);
        // 展示编辑窗口
        displayEditView(true);
    }

    public boolean editActionUpdate() {
        JsonRecord record = getCurrentSelectionValue();
        return showTree.isEnabled() && Objects.nonNull(record);
    }


    public void executeAddAction() {
        // 展示编辑窗口
        displayEditView(false);
    }

    public boolean addActionUpdate() {
        return showTree.isEnabled();
    }

    public void executeRemoveAction() {
        List<Integer> ids = new ArrayList<>();
        if (HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode()) {
            ids.add(showList.getSelectedValue().getId());

        } else {
            Optional.ofNullable(showTree.getSelectionPath())
                    .map(TreePath::getLastPathComponent)
                    .map(el -> (HistoryTreeNode) el)
                    .ifPresent(node -> {
                        if (HistoryTreeNodeType.NODE == node.getNodeType()) {
                            ids.add(node.getValue().getId());

                        } else {
                            // 把子节点的id都添加进去
                            List<TreeNode> nodeList = JsonAssistantUtil.enumerationToList(node.children());
                            for (TreeNode treeNode : nodeList) {
                                HistoryTreeNode childNode = (HistoryTreeNode) treeNode;
                                ids.add(childNode.getValue().getId());
                            }
                        }
                    });
        }

        historyManager.batchRemove(ids);
        // 刷新
        refreshHistoryComponent();
        // 刷新自动完成列表
        completeWrapper.setVariants(getLatestVariants());
    }

    public boolean removeActionUpdate() {
        return showTree.isEnabled()
                && (HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode()
                ? null != showList.getSelectedValue()
                : showTree.getSelectionCount() > 0);
    }


    public JsonRecord getCurrentSelectionValue() {
        // 获取当前显示的样式
        HistoryDisplayMode mode = historyState.getHistoryDisplayMode();
        // 获取选中的元素
        JsonRecord record;
        if (HistoryDisplayMode.LIST == mode) {
            record = showList.getSelectedValue();
        } else {
            record = Optional.ofNullable(showTree.getSelectionPath())
                    .map(TreePath::getLastPathComponent)
                    .map(el -> (HistoryTreeNode) el)
                    .map(HistoryTreeNode::getValue)
                    .orElse(null);
        }

        return record;
    }

    // TODO 当用户选择 “指定名称” 时，打开此工具窗，打开更新页面，定位到指定记录，并且把焦点放在名称编辑器上

    // TODO 还差一个导入按钮，或者不要也可以

    // ----------------------------------- 逻辑 -----------------------------------

    private void addRecord() {
        JsonParseResult result = parseAndValidateContent();
        if (null == result) return;

        JsonRecord record = new JsonRecord()
                .setRawText(result.content)
                .setSourceType(result.formatType)
                .setWrapper(result.wrapper);

        String recordName = result.recordName;
        if (StrUtil.isNotBlank(recordName)) record.setName(recordName);

        historyManager.addEntry(record);

        // 退出编辑模式
        dismissEditView();

        // 刷新列表/树
        refreshHistoryComponent();

        // 刷新自动完成列表
        completeWrapper.setVariants(getLatestVariants());

        // 获取焦点
        requestFocusOnCurrentViewComponent();
    }

    private void updateRecord(JsonRecord record) {
        JsonParseResult result = parseAndValidateContent();
        if (null == result) return;

        // 3.保存
        record.setRawText(result.content)
                .setSourceType(result.formatType)
                .setUpdateTime(System.currentTimeMillis())
                .setWrapper(result.wrapper)
                .setDisplayText(HistoryManager.getShortText(result.wrapper));

        String recordName = result.recordName;
        if (StrUtil.isNotBlank(recordName)) record.setName(recordName);

        // 4.退出编辑模式
        dismissEditView();

        // 5.刷新列表/树
        refreshHistoryComponent();

        // 刷新自动完成列表
        completeWrapper.setVariants(getLatestVariants());

        // 6.获取焦点
        requestFocusOnCurrentViewComponent();
    }

    private JsonParseResult parseAndValidateContent() {
        // 1.判断Json编辑器内是否是正确文本
        String content = StrUtil.trim(recordEditor.getDocument().getText());
        // 检查内容有效性
        if (StrUtil.isBlank(content)) {
            HintManager.getInstance().showErrorHint(recordEditor, JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return null;
        }

        // 解析格式
        JsonWrapper wrapper = null;
        DataFormatType formatType = DataFormatType.JSON;
        if (JsonUtil.isJson(content)) {
            wrapper = JsonUtil.parse(content);

        } else if (Json5Util.isJson5(content)) {
            formatType = DataFormatType.JSON5;
            wrapper = Json5Util.parse(content);
            // 由这里再进行格式化（不可避免会去掉一些Array上的注释）
            content = Json5Util.formatJson5WithComment(content);
        }

        if (null == wrapper || wrapper.noItems()) {
            moveToErrorElementOffset(recordEditor);
            HintManager.getInstance().showErrorHint(recordEditor, JsonAssistantBundle.messageOnSystem("error.invalid.json"));
            return null;
        }

        // 2.名称有没有超过限制（不能多于50个字符，不能与其他记录重名）
        String name = nameEditorWrapper.getText();
        Editor nameEditor = nameEditorWrapper.getEditor();
        if (StrUtil.isNotBlank(name) && name.length() > 50) {
            moveToErrorElementOffset(nameEditor);
            HintManager.getInstance().showErrorHint(nameEditor, JsonAssistantBundle.messageOnSystem("hint.history.invalid.name"));
            return null;
        }

        if (null != historyManager.findByName(name)) {
            moveToErrorElementOffset(nameEditor);
            HintManager.getInstance().showErrorHint(nameEditor, JsonAssistantBundle.messageOnSystem("hint.history.same.name"));
            return null;
        }

        return new JsonParseResult(content, name, formatType, wrapper);
    }


    private void moveToErrorElementOffset(Editor editor) {
        Document document = editor.getDocument();
        PsiFile psiFile = PlatformUtil.getPsiFile(project, document);
        PsiErrorElement errorElement = PsiTreeUtil.findChildOfType(psiFile, PsiErrorElement.class);

        if (null != errorElement) {
            int textOffset = errorElement.getTextOffset();
            editor.getCaretModel().moveToOffset(textOffset);
        }
    }

    private void displayEditView(boolean isUpdate) {
        // 可编辑
        ((EditorEx) recordEditor).setViewer(false);
        // 展示编辑框和按钮
        updatePanel.setVisible(true);

        if (isUpdate) {
            updateButton.setVisible(true);
            updatePanel.getRootPane().setDefaultButton(updateButton);
            IdeFocusManager.findInstance().requestFocus(nameEditorWrapper.getPreferredFocusedComponent(), true);
            nameEditorWrapper.moveToOffset(nameEditorWrapper.getText().length());
        } else {
            addButton.setVisible(true);
            updatePanel.getRootPane().setDefaultButton(addButton);
            // 清空编辑器
            clearEditor();
            IdeFocusManager.findInstance().requestFocus(recordEditor.getComponent(), true);
        }

        showList.setEnabled(false);
        showTree.setEnabled(false);

        showList.repaint();
        showTree.repaint();
    }

    private void dismissEditView() {
        // 不可编辑
        ((EditorEx) recordEditor).setViewer(true);
        JsonRecord currentSelectionValue = getCurrentSelectionValue();
        if (currentSelectionValue == null) {
            clearEditor();
        } else {
            refreshEditor(currentSelectionValue);
        }

        nameEditorWrapper.setText("");
        updatePanel.setVisible(false);
        addButton.setVisible(false);
        updateButton.setVisible(false);

        showList.setEnabled(true);
        showTree.setEnabled(true);

        showList.repaint();
        showTree.repaint();
    }

    private void requestFocusOnCurrentViewComponent() {
        // 获取焦点
        JComponent component = HistoryDisplayMode.LIST == historyState.getHistoryDisplayMode() ? showList : showTree;
        IdeFocusManager.findInstance().requestFocus(component, true);
    }

    private void refreshEditor(JsonRecord record) {
        if (record != null) {
            Document document = recordEditor.getDocument();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PlatformUtil.setDocumentText(document, record.getRawText());
                refreshDocument(document);
            });
        }
    }

    private void refreshDocument(Document document) {
        // -------------- 重新绘制
        int newLineCount = document.getLineCount();
        if (lastLineCount != newLineCount) {
            lastLineCount = newLineCount;
            UIUtils.repaintEditor(recordEditor);
        }
    }

    private void clearEditor() {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            Document document = recordEditor.getDocument();
            PlatformUtil.setDocumentText(document, "");
            refreshDocument(document);
        });
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(recordEditor);
        Disposer.dispose(this);
    }

    static class SavedNodeInfo {

        /**
         * 是否选中节点
         */
        boolean isSelected;

        /**
         * 是否为组节点
         */
        boolean isGroup;

        /**
         * 组节点的日期（组节点时使用）
         */
        String groupDate;

        /**
         * 组节点在根节点中的索引
         */
        int groupIndex = -1;

        /**
         * 记录节点的唯一ID（记录节点时使用）
         */
        Integer recordId;

        /**
         * 记录节点在组内的索引
         */
        int recordIndex = -1;

    }

    static class JsonParseResult {

        final String content;
        final String recordName;
        final DataFormatType formatType;
        final JsonWrapper wrapper;

        public JsonParseResult(String content, String recordName, DataFormatType formatType, JsonWrapper wrapper) {
            this.content = content;
            this.recordName = recordName;
            this.formatType = formatType;
            this.wrapper = wrapper;
        }

    }


}
