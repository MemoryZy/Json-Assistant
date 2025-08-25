package cn.memoryzy.json.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.history.AddHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.EditHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.RemoveHistoryAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.enums.HistoryAffectType;
import cn.memoryzy.json.enums.HistoryDisplayMode;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.event.*;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.HistoryState;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.decorator.TextEditorErrorPopupDecorator;
import cn.memoryzy.json.ui.editor.EditExtension;
import cn.memoryzy.json.ui.list.FilterableList;
import cn.memoryzy.json.ui.listener.history.AddAction;
import cn.memoryzy.json.ui.listener.history.CancelAction;
import cn.memoryzy.json.ui.listener.history.UpdateAction;
import cn.memoryzy.json.ui.tree.HistoryFilterableTree;
import cn.memoryzy.json.ui.tree.HistoryNode;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
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
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.speedSearch.FilteringListModel;
import com.intellij.ui.speedSearch.NameFilteringListModel;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public static final Key<Boolean> EDIT_MODE_FLAG = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".EDIT_MODE_FLAG");

    private final Project project;
    private final HistoryManager historyManager;
    private final HistoryState historyState;

    /**
     * 消息总线（项目级）
     */
    private final MessageBusConnection projectConnection;
    private final MessageBus applicationMessageBus = ApplicationManager.getApplication().getMessageBus();
    private final SimpleToolWindowPanel windowPanel;

    // ----------------------- left
    private final JBCardLayout cardLayout;
    private final JPanel cardPanel;

    // ------------------ List
    private final JBList<JsonRecord> showList;
    private final FilterableList<JsonRecord> filterableList;
    private final SearchTextField listFilterField;

    // ------------------ Tree
    private final Tree showTree;
    private final HistoryFilterableTree filterableTree;
    private final SearchTextField treeFilterField;

    // ----------------------- right
    private final Editor recordEditor;
    private final JPanel updatePanel;
    private final ExtendableTextField nameTextField;
    private TextEditorErrorPopupDecorator nameDecorator;
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
        // --------------- List
        this.showList = new JBList<>(createListModel());
        this.filterableList = new FilterableList<>(showList,
                el -> (StrUtil.isNotBlank(el.getName()) ? el.getName() : el.getDisplayText()), false);
        this.listFilterField = filterableList.configureBorderlessFilterField();

        // --------------- Tree
        this.filterableTree = new HistoryFilterableTree(project, new HistoryNode().setNodeType(HistoryTreeNodeType.ROOT));
        this.showTree = filterableTree.getTree();
        this.treeFilterField = filterableTree.installBorderlessSearchField();

        // ----------------------- right
        this.recordEditor = createJsonEditor();
        this.nameTextField = createNameTextField();
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

        // 注册事件处理器
        registerEventHandlers();

        windowPanel.setToolbar(createToolbar(windowPanel));
        windowPanel.setContent(splitter);
        return windowPanel;
    }

    private JComponent createFirstComponent() {
        // CardLayout 切换 Tree 和 JBList 展示
        JPanel listPanel = createListPanel();
        JPanel treePanel = createTreePanel();

        cardPanel.add(listPanel, HistoryDisplayMode.LIST.name());
        cardPanel.add(treePanel, HistoryDisplayMode.TREE.name());

        // 默认显示
        cardLayout.show(cardPanel, historyState.getHistoryDisplayMode().name());

        return cardPanel;
    }

    private JComponent createSecondComponent() {
        configureUpdatePanel();

        return new BorderLayoutPanel()
                .addToCenter(recordEditor.getComponent())
                .addToBottom(updatePanel);
    }


    private JPanel createListPanel() {
        // 上方编辑器，下方列表
        showList.setFont(UIUtils.getFontForCurrentUi(13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.setCellRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends JsonRecord> list, JsonRecord value, int index, boolean selected, boolean hasFocus) {
                String name = value.getName();
                append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
                append((StrUtil.isNotBlank(name) ? name : value.getDisplayText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                setIcon(AllIcons.FileTypes.Json);

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

        JBTextField editor = listFilterField.getTextEditor();
        editor.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));
        editor.getAccessibleContext().setAccessibleName(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));

        return new BorderLayoutPanel()
                .addToTop(new BorderLayoutPanel().addToTop(listFilterField).addToCenter(new JSeparator()))
                .addToCenter(UIUtils.wrapScrollPane(showList));
    }

    private JPanel createTreePanel() {
        showTree.setDragEnabled(true);
        showTree.setExpandableItemsEnabled(true);
        showTree.setRootVisible(false);
        // 设置单击展开节点
        showTree.setToggleClickCount(1);
        showTree.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
        showTree.setFont(UIUtils.getFontForCurrentUi(13));
        showTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        showTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                HistoryNode historyNode = (HistoryNode) node.getUserObject();
                HistoryTreeNodeType nodeType = historyNode.getNodeType();

                if (HistoryTreeNodeType.NODE.equals(nodeType)) {
                    setIcon(AllIcons.FileTypes.Json);
                    append(historyNode.toString());
                } else {
                    setIcon(JsonAssistantIcons.GROUP);
                    append(historyNode + " (" + historyNode.getSize() + ")");
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

                // SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
            }
        });

        showTree.addTreeSelectionListener(e -> {
            if (HistoryDisplayMode.TREE == historyState.getHistoryDisplayMode()) {
                TreePath selectionPath = showTree.getSelectionPath();
                if (selectionPath != null) {
                    HistoryNode historyNode = filterableTree.getUserObject(selectionPath);
                    if (HistoryTreeNodeType.GROUP.equals(historyNode.getNodeType())) {
                        clearEditor();
                    } else {
                        refreshEditor(historyNode.getValue());
                    }
                }
            }
        });

        TreeUtil.installActions(showTree);
        // 确保选择
        TreeUtil.ensureSelection(showTree);

        JBTextField editor = treeFilterField.getTextEditor();
        editor.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));
        editor.getAccessibleContext().setAccessibleName(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));

        return new BorderLayoutPanel()
                .addToTop(new BorderLayoutPanel().addToTop(treeFilterField).addToCenter(new JSeparator()))
                .addToCenter(UIUtils.wrapScrollPane(showTree));
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

    private ExtendableTextField createNameTextField() {
        ExtendableTextField extendableTextField = new ExtendableTextField(15);
        extendableTextField.setExtensions(new EditExtension());
        return extendableTextField;
    }

    private void configureUpdatePanel() {
        nameDecorator = new TextEditorErrorPopupDecorator(windowPanel.getRootPane(), nameTextField);

        StatusText emptyText = nameTextField.getEmptyText();
        emptyText.setText("Name");
        nameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && updatePanel.isVisible()) {
                    dismissEditView();
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(3); // 组件间距

        // 1. 左侧输入框（占据剩余空间）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // 关键：水平权重
        updatePanel.add(nameTextField, gbc);

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

    private DefaultListModel<JsonRecord> createListModel() {
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();
        return JBList.createDefaultListModel(recentHistories);
    }

    private Editor createJsonEditor() {
        EditorEx editor = (EditorEx) PlatformUtil.createEditor(project, PluginConstant.HISTORY_EDITOR_NAME, FileTypeHolder.JSON5, true, EditorKind.MAIN_EDITOR, "");
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
        // 添加标记表示不处于编辑模式
        editor.putUserData(EDIT_MODE_FLAG, Boolean.FALSE);

        // 添加ESC键监听
        editor.getContentComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && updatePanel.isVisible()) {
                    dismissEditView();
                }
            }
        });

        // 触发工具栏的展示（203版本）
        if (JsonAssistantPlugin.LEGACY_FLOATING_TOOLBAR_PROVIDER) {
            editor.addFocusListener(new FocusChangeListener() {
                @Override
                public void focusGained(@NotNull Editor editor) {
                    applicationMessageBus.syncPublisher(HistoryEditorFocusGainedEvent.TOPIC).focusGained(editor);
                }
            });
        }

        return editor;
    }

    public JComponent getPreferredFocusedComponent() {
        return HistoryDisplayMode.TREE == historyState.getHistoryDisplayMode() ? showTree : showList;
    }


    /**
     * 注册事件处理器
     */
    private void registerEventHandlers() {
        // 注册配置更新事件
        JsonAssistantToolWindowComponentProvider.APPLICATION_CONNECTION.subscribe(HistoryViewChangedEvent.TOPIC, (HistoryViewChangedEvent) this::applyViewMode);
        // 注册历史记录添加事件
        projectConnection.subscribe(HistoryAddedEvent.TOPIC, (HistoryAddedEvent) () -> refreshHistoryComponent(HistoryAffectType.ADD));
        // 注册精确找到记录事件
        projectConnection.subscribe(NavigateRecordEvent.TOPIC, (NavigateRecordEvent) this::navigateRecord);
    }

    /**
     * 切换显示模式
     *
     * @param mode 新模式
     */
    private void applyViewMode(HistoryDisplayMode mode) {
        // 把先前的文本设置给后面的编辑器
        if (HistoryDisplayMode.TREE == mode) {
            treeFilterField.setText(listFilterField.getText());
        } else {
            listFilterField.setText(treeFilterField.getText());
        }

        cardLayout.show(cardPanel, mode.name());
    }

    /**
     * 定位到具体的记录
     *
     * @param recordId   记录ID
     * @param shouldEdit 是否编辑
     */
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
        NameFilteringListModel<JsonRecord> model = (NameFilteringListModel<JsonRecord>) showList.getModel();
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
                    IdeFocusManager.findInstance().requestFocus(nameTextField, true);
                } else {
                    // 直接给记录组件请求焦点
                    requestFocusOnStructureComponent();
                }

                break;
            }
        }
    }

    private void navigateInTree(Integer recordId, boolean shouldEdit) {
        // 遍历树查找记录
        DefaultMutableTreeNode recordNode = findRecordNode(recordId);

        if (recordNode != null) {
            // 确保路径可见并选中
            TreePath recordPath = new TreePath(recordNode.getPath());
            showTree.expandPath(recordPath.getParentPath());
            showTree.setSelectionPath(recordPath);
            showTree.scrollPathToVisible(recordPath);

            // 如果需要编辑
            if (shouldEdit) {
                executeEditAction();
                // 名称编辑器获得焦点
                IdeFocusManager.findInstance().requestFocus(nameTextField, true);
            } else {
                // 直接给记录组件请求焦点
                requestFocusOnStructureComponent();
            }
        } else {
            // 未找到记录
            clearEditor();
        }
    }

    /**
     * 刷新历史记录组件
     *
     * @param affectType 记录影响类型
     */
    public void refreshHistoryComponent(HistoryAffectType affectType) {
        // 去除过滤文本
        listFilterField.setText(null);
        treeFilterField.setText(null);

        refreshListComponent(affectType);
        refreshTreeComponent(affectType);
    }

    /**
     * 刷新列表组件
     *
     * @param affectType 记录影响类型
     */
    private void refreshListComponent(HistoryAffectType affectType) {
        // 获取当前选中的索引（操作前的索引）
        int selectedIndex = showList.getSelectedIndex();
        // 替换数据
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();
        filterableList.replaceAll(recentHistories);
        FilteringListModel<JsonRecord> model = (FilteringListModel<JsonRecord>) showList.getModel();

        // 计算新的选中索引
        int newIndex = -1;
        if (CollUtil.isNotEmpty(recentHistories)) {
            switch (affectType) {
                case ADD:
                    // 如果是添加，索引需要往后挪一位，保持原来的选中
                    if (selectedIndex >= 0) {
                        newIndex = selectedIndex + 1;
                    } else {
                        // 如果没有选中项，默认选中第一个
                        newIndex = 0;
                    }
                    break;
                case REMOVE:
                    // 如果是删除，则选中原先的下一位元素
                    if (selectedIndex >= 0) {
                        // 如果删除的不是最后一项，选中下一项；否则选中前一项
                        newIndex = (selectedIndex < model.getSize()) ? selectedIndex : selectedIndex - 1;
                    } else {
                        // 如果没有选中项，默认选中第一个
                        newIndex = 0;
                    }
                    break;
                case UPDATE:
                default:
                    // 更新操作保持原选中位置
                    if (selectedIndex >= 0 && selectedIndex < model.getSize()) {
                        newIndex = selectedIndex;
                    } else if (selectedIndex >= model.getSize()) {
                        newIndex = model.getSize() - 1;
                    } else {
                        newIndex = 0;
                    }
                    break;
            }

            // 确保索引在有效范围内
            newIndex = Math.max(0, Math.min(newIndex, model.getSize() - 1));
        }

        // 设置新的选中项
        if (newIndex >= 0 && newIndex < model.getSize()) {
            showList.setSelectedIndex(newIndex);
            showList.scrollRectToVisible(showList.getCellBounds(newIndex, newIndex));
        } else {
            // 确保列表始终存在有效的选中项
            ScrollingUtil.ensureSelectionExists(showList);
        }
    }


    /**
     * 刷新树组件
     *
     * @param affectType 记录影响类型
     */
    private void refreshTreeComponent(HistoryAffectType affectType) {
        // 保存当前选中节点和展开状态（基于内容而非节点对象）
        List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(showTree);
        // 收集当前展开的分组节点日期（能展开的只有组）
        List<String> expandedGroupDates = new ArrayList<>();
        for (TreePath expandedPath : expandedPaths) {
            HistoryNode node = filterableTree.getUserObject(expandedPath);
            if (!node.isGroup()) continue;
            expandedGroupDates.add(node.toString());
        }

        // 保存选中节点的信息以便后续定位
        String selectedGroupDate = null;
        Integer selectedRecordId = null;
        int selectedChildIndex = -1;
        boolean wasGroupSelected = false;

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) showTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            HistoryNode selectedHistoryNode = (HistoryNode) selectedNode.getUserObject();

            if (selectedHistoryNode.getNodeType() == HistoryTreeNodeType.GROUP) {
                // 选中的是分组节点
                selectedGroupDate = selectedHistoryNode.toString();
                wasGroupSelected = true;
            } else {
                // 选中的是记录节点
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
                if (parentNode != null) {
                    HistoryNode parentHistoryNode = (HistoryNode) parentNode.getUserObject();
                    selectedGroupDate = parentHistoryNode.toString(); // 分组日期

                    // 获取选中节点在父节点中的索引
                    selectedChildIndex = parentNode.getIndex(selectedNode);

                    // 假设HistoryNode有一个获取记录ID的方法
                    // 如果没有，可以使用其他唯一标识符
                    selectedRecordId = selectedHistoryNode.getValue().getId();
                }
            }
        }

        // 重新构建节点
        filterableTree.update();

        // 恢复展开状态 - 基于之前收集的分组日期
        DefaultMutableTreeNode root = filterableTree.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
            HistoryNode groupHistoryNode = (HistoryNode) groupNode.getUserObject();

            if (expandedGroupDates.contains(groupHistoryNode.toString())) {
                showTree.expandPath(new TreePath(groupNode.getPath()));
            }
        }

        // 根据操作类型处理选中状态
        DefaultMutableTreeNode newSelectedNode = null;
        switch (affectType) {
            case ADD:
                // 添加操作：尝试恢复原来的选中节点
                if (wasGroupSelected && selectedGroupDate != null) {
                    // 原来是选中分组节点
                    newSelectedNode = findGroupNode(selectedGroupDate);
                } else if (selectedRecordId != null) {
                    // 原来是选中记录节点
                    newSelectedNode = findRecordNode(selectedRecordId);
                }
                break;

            case REMOVE:
                // 删除操作：选中同组的下一个节点或上一个节点
                if (selectedGroupDate != null && selectedChildIndex >= 0) {
                    // 查找分组节点
                    DefaultMutableTreeNode groupNode = findGroupNode(selectedGroupDate);
                    if (groupNode != null && groupNode.getChildCount() > 0) {
                        // 计算新的选中索引
                        int newIndex = selectedChildIndex;
                        if (newIndex >= groupNode.getChildCount()) {
                            newIndex = groupNode.getChildCount() - 1;
                        }

                        // 选中新的节点
                        if (newIndex >= 0) {
                            newSelectedNode = (DefaultMutableTreeNode) groupNode.getChildAt(newIndex);
                        }
                    }
                }
                break;

            case UPDATE:
            default:
                // 更新操作：尝试恢复原来的选中节点
                if (wasGroupSelected && selectedGroupDate != null) {
                    newSelectedNode = findGroupNode(selectedGroupDate);
                } else if (selectedRecordId != null) {
                    newSelectedNode = findRecordNode(selectedRecordId);
                }
                break;
        }

        // 设置新的选中项
        if (newSelectedNode != null) {
            TreePath newPath = new TreePath(newSelectedNode.getPath());
            showTree.setSelectionPath(newPath);
            showTree.scrollPathToVisible(newPath);
        } else {
            // 确保树始终存在有效的选中项
            TreeUtil.ensureSelection(showTree);
        }
    }

    /**
     * 根据日期查找分组节点
     */
    private DefaultMutableTreeNode findGroupNode(String groupDate) {
        DefaultMutableTreeNode root = filterableTree.getRoot();

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
            HistoryNode groupHistoryNode = (HistoryNode) groupNode.getUserObject();

            if (groupDate.equals(groupHistoryNode.toString())) {
                return groupNode;
            }
        }

        return null;
    }

    /**
     * 根据记录ID查找记录节点
     *
     * @param recordId 记录ID
     * @return 记录节点
     */
    private DefaultMutableTreeNode findRecordNode(Integer recordId) {
        DefaultMutableTreeNode root = filterableTree.getRoot();

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);

            for (int j = 0; j < groupNode.getChildCount(); j++) {
                DefaultMutableTreeNode recordNode = (DefaultMutableTreeNode) groupNode.getChildAt(j);
                HistoryNode recordHistoryNode = (HistoryNode) recordNode.getUserObject();

                // 假设HistoryNode有一个获取记录ID的方法
                // 如果没有，可以使用其他唯一标识符进行比较
                if (recordId.equals(recordHistoryNode.getValue().getId())) {
                    return recordNode;
                }
            }
        }

        return null;
    }


    public void executeEditAction() {
        // 获取当前选中的元素
        JsonRecord record = getCurrentSelectionValue();
        // 名称
        nameTextField.setText(record.getName());
        ((UpdateAction) updateButton.getAction()).setRecord(record);
        // 展示编辑窗口
        displayEditView(true);
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
                    .map(el -> ((HistoryNode) ((DefaultMutableTreeNode) el).getUserObject()))
                    .map(HistoryNode::getValue)
                    .orElse(null);
        }

        return record;
    }

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
        refreshHistoryComponent(HistoryAffectType.ADD);

        // 获取焦点
        requestFocusOnStructureComponent();
    }

    private void updateRecord(JsonRecord record) {
        JsonParseResult result = parseAndValidateContent();
        if (null == result) return;

        // 3.保存
        record.setRawText(result.content)
                .setSourceType(result.formatType)
                .setName(result.recordName)
                .setUpdateTime(System.currentTimeMillis())
                .setWrapper(result.wrapper)
                .setDisplayText(HistoryManager.getShortText(result.wrapper));

        // 4.退出编辑模式
        dismissEditView();

        // 5.刷新列表/树
        refreshHistoryComponent(HistoryAffectType.UPDATE);

        // 6.获取焦点
        requestFocusOnStructureComponent();
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
        String name = nameTextField.getText();
        if (StrUtil.isNotBlank(name) && name.length() > 50) {
            nameDecorator.setError(JsonAssistantBundle.messageOnSystem("hint.history.invalid.name"));
            return null;
        }

        if (null != historyManager.findByName(name)) {
            nameDecorator.setError(JsonAssistantBundle.messageOnSystem("hint.history.same.name"));
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

    /**
     * 切换为编辑模式
     *
     * @param isUpdate 是否为更新
     */
    public void displayEditView(boolean isUpdate) {
        // 可编辑
        ((EditorEx) recordEditor).setViewer(false);
        // 展示编辑框和按钮
        updatePanel.setVisible(true);

        if (isUpdate) {
            updateButton.setVisible(true);
            updatePanel.getRootPane().setDefaultButton(updateButton);
            IdeFocusManager.findInstance().requestFocus(recordEditor.getContentComponent(), true);
        } else {
            addButton.setVisible(true);
            updatePanel.getRootPane().setDefaultButton(addButton);
            // 清空编辑器
            clearEditor();
            IdeFocusManager.findInstance().requestFocus(recordEditor.getContentComponent(), true);
        }

        showList.setEnabled(false);
        showTree.setEnabled(false);

        showList.repaint();
        showTree.repaint();

        // 添加标记表示处于编辑模式
        recordEditor.putUserData(EDIT_MODE_FLAG, Boolean.TRUE);

        applicationMessageBus.syncPublisher(HistoryWindowEditEvent.TOPIC).handle(recordEditor);
    }

    /**
     * 关闭编辑模式
     */
    private void dismissEditView() {
        // 不可编辑
        ((EditorEx) recordEditor).setViewer(true);
        JsonRecord currentSelectionValue = getCurrentSelectionValue();
        if (currentSelectionValue == null) {
            clearEditor();
        } else {
            refreshEditor(currentSelectionValue);
        }

        nameTextField.setText("");
        updatePanel.setVisible(false);
        addButton.setVisible(false);
        updateButton.setVisible(false);

        showList.setEnabled(true);
        showTree.setEnabled(true);

        showList.repaint();
        showTree.repaint();

        // 添加标记表示不处于编辑模式
        recordEditor.putUserData(EDIT_MODE_FLAG, Boolean.FALSE);

        applicationMessageBus.syncPublisher(HistoryWindowExitEditEvent.TOPIC).handle(recordEditor);
    }

    public void requestFocusOnStructureComponent() {
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


    // ------------------------- GETTER ------------------------- //

    public ExtendableTextField getNameTextField() {
        return nameTextField;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public Tree getTree() {
        return showTree;
    }

    public JBList<JsonRecord> getList() {
        return showList;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public HistoryState getHistoryState() {
        return historyState;
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
