package cn.memoryzy.json.ui;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.action.toolwindow.history.AddHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.EditHistoryAction;
import cn.memoryzy.json.action.toolwindow.history.RemoveHistoryAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.HistoryDisplayMode;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.event.HistoryViewChangedEvent;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.service.persistent.v2.HistoryManager;
import cn.memoryzy.json.service.persistent.v2.ToolWindowSettings;
import cn.memoryzy.json.ui.node.HistoryTreeNode2;
import cn.memoryzy.json.ui.panel.AutoCompleteWrapper;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.time.LocalDate;
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

    private final Project project;
    private final HistoryManager historyManager;
    private final HistoryState historyState;

    // ----------------------- left
    private final AutoCompleteWrapper completeWrapper;
    private final JBCardLayout cardLayout;
    private final JPanel cardPanel;
    private final JBList<JsonRecord> showList;
    private final Tree showTree;

    // ----------------------- right
    private final EditorTextField nameTextField;
    private final BorderLayoutPanel namePanel;
    private final Editor recordEditor;
    private final JPanel updateButtonPanel;
    private final JButton addButton;
    private final JButton updateButton;
    private final JButton cancelButton;

    public HistoryToolWindowComponentProvider(Project project) {
        this.project = project;
        this.historyManager = HistoryManager.getInstance(project);
        this.historyState = ToolWindowSettings.getInstance().getHistoryState();

        // ----------------------- left
        this.cardLayout = new JBCardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.showList = new JBList<>(createListModel());
        this.showTree = new Tree(createTreeModel());
        this.completeWrapper = new AutoCompleteWrapper(project, getLatestVariants(), () -> PluginConstant.HISTORY_SEARCH_HISTORY_KEY);

        // ----------------------- right
        this.recordEditor = createJsonEditor();
        this.nameTextField = createNameTextField();
        this.namePanel = new BorderLayoutPanel();
        this.addButton = new JButton(JsonAssistantBundle.messageOnSystem("toolwindow.history.add.button"));
        this.updateButton = new JButton(JsonAssistantBundle.messageOnSystem("toolwindow.history.update.button"));
        this.cancelButton = new JButton(JsonAssistantBundle.messageOnSystem("toolwindow.history.cancel.button"));
        this.updateButtonPanel = new JPanel();
    }

    public JComponent createComponent() {
        JBSplitter splitter = new JBSplitter(false, 0.3f);
        splitter.setAndLoadSplitterProportionKey(SPLITTER_PROPORTION_KEY);
        splitter.setFirstComponent(createFirstComponent());
        splitter.setSecondComponent(createSecondComponent());

        SimpleToolWindowPanel windowPanel = new SimpleToolWindowPanel(false, false);
        windowPanel.setToolbar(createToolbar(windowPanel));
        windowPanel.setContent(splitter);
        return windowPanel;
    }

    private JComponent createToolbar(SimpleToolWindowPanel windowPanel) {
        SimpleActionGroup actionGroup = new SimpleActionGroup();
        actionGroup.add(new AddHistoryAction(this));
        actionGroup.add(new RemoveHistoryAction(this));
        actionGroup.add(new EditHistoryAction(this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true);
        toolbar.setTargetComponent(windowPanel);
        return toolbar.getComponent();
    }

    private JComponent createFirstComponent() {
        // CardLayout 切换 Tree 和 JBList 展示
        JScrollPane listScrollPane = createListScrollPane();
        JScrollPane treeScrollPane = createTreeScrollPane();

        cardPanel.add(listScrollPane, UIUtils.HISTORY_LIST_CARD_NAME);
        cardPanel.add(treeScrollPane, UIUtils.HISTORY_TREE_CARD_NAME);

        // 默认显示
        cardLayout.show(cardPanel, getViewMode(historyState.getHistoryDisplayMode()));

        registerConfigurationUpdateEventHandlers();

        // TODO 当点击修改按钮时，把列表隐藏，展示一个输入框、一个编辑器，在其中编辑名称及json，还有一个按钮
        return new BorderLayoutPanel().addToTop(completeWrapper).addToCenter(cardPanel);
    }

    private JComponent createSecondComponent() {
        // 名称编辑器
        namePanel.addToCenter(nameTextField);
        namePanel.setBorder(JBUI.Borders.empty(2, 0, 5, 0));

        // 指定按钮位置
        updateButtonPanel.setLayout(new BoxLayout(updateButtonPanel, BoxLayout.X_AXIS));
        updateButtonPanel.add(Box.createHorizontalGlue()); // 左侧胶水撑开空间
        updateButtonPanel.add(addButton);
        updateButtonPanel.add(Box.createHorizontalStrut(2)); // 按钮间距
        updateButtonPanel.add(updateButton);
        updateButtonPanel.add(Box.createHorizontalStrut(2)); // 按钮间距
        updateButtonPanel.add(cancelButton);
        updateButtonPanel.setBorder(JBUI.Borders.empty(5, 0, 5, 8));

        // 默认隐藏
        namePanel.setVisible(false);
        updateButtonPanel.setVisible(false);

        return new BorderLayoutPanel()
                .addToTop(namePanel)
                .addToCenter(recordEditor.getComponent())
                .addToBottom(updateButtonPanel);
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
                SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
            }
        });
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
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
                HistoryTreeNode2 treeNode = (HistoryTreeNode2) value;
                HistoryTreeNodeType nodeType = treeNode.getNodeType();

                if (HistoryTreeNodeType.NODE.equals(nodeType)) {
                    setIcon(AllIcons.FileTypes.Json);
                    append(treeNode.toString());
                } else {
                    setIcon(JsonAssistantIcons.GROUP);
                    append(treeNode + " (" + treeNode.getSize() + ")");
                }

                SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
            }
        });

        new TreeSpeedSearch(showTree);
        return UIUtils.wrapScrollPane(showTree);
    }

    private EditorTextField createNameTextField() {
        EditorTextField editorTextField = new EditorTextField(project, PlainTextFileType.INSTANCE);
        editorTextField.setPlaceholder("Name");
        return editorTextField;
    }

    private void registerConfigurationUpdateEventHandlers() {
        ToolWindowUtil.APPLICATION_CONNECTION.subscribe(HistoryViewChangedEvent.TOPIC, (HistoryViewChangedEvent) this::applyViewMode);
    }

    private void applyViewMode(HistoryDisplayMode mode) {
        cardLayout.show(cardPanel, getViewMode(mode));
    }

    private DefaultListModel<JsonRecord> createListModel() {
        List<JsonRecord> recentHistories = historyManager.getRecentHistories();
        return JBList.createDefaultListModel(recentHistories);
    }

    private TreeModel createTreeModel() {
        return new DefaultTreeModel(combineRootTreeNode());
    }

    private TreeNode combineRootTreeNode() {
        HistoryTreeNode2 rootNode = new HistoryTreeNode2();
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
            HistoryTreeNode2 groupNode = new HistoryTreeNode2(null, key, value.size(), HistoryTreeNodeType.GROUP);

            // 添加底层数据节点
            for (JsonRecord record : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryTreeNode2(record, null, null, HistoryTreeNodeType.NODE));
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
        settings.setFoldingOutlineShown(true);
        // 折叠块、行号所展示的区域
        settings.setLineMarkerAreaShown(false);

        ErrorStripeEditorCustomization.DISABLED.customize(editor);
        Objects.requireNonNull(SpellCheckingEditorCustomizationProvider.getInstance().getDisabledCustomization()).customize(editor);

        // 设置绘画背景
        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        gutterComponentEx.setPaintBackground(false);

        editor.setBorder(JBUI.Borders.empty());

        JComponent component = editor.getComponent();
        component.setFont(UIUtils.consolasFont(15));
        component.setBorder(JBUI.Borders.customLine(editor.getBackgroundColor(), 0, 4, 0, 0));

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

    private String getViewMode(HistoryDisplayMode mode) {
        return HistoryDisplayMode.LIST == mode ? UIUtils.HISTORY_LIST_CARD_NAME : UIUtils.HISTORY_TREE_CARD_NAME;
    }

    @SuppressWarnings("DataFlowIssue")
    public void executeEditAction(boolean isUpdate) {
//        // 获取当前显示的样式
//        HistoryDisplayMode mode = historyState.getHistoryDisplayMode();
//        // 获取选中的元素
//        JsonRecord record;
//        if (HistoryDisplayMode.LIST == mode) {
//            record = showList.getSelectedValue();
//        } else {
//            HistoryTreeNode2 treeNode = (HistoryTreeNode2) showTree.getSelectionPath().getLastPathComponent();
//            record = treeNode.getValue();
//        }

        displayEditView(isUpdate);
    }




    // TODO 当用户选择 “指定名称” 时，打开此工具窗，打开更新页面，定位到指定记录，并且把焦点放在名称编辑器上

    // TODO 还差一个导入按钮，或者不要也可以

    // ----------------------------------- 逻辑 -----------------------------------

    private void updateRecord() {

    }

    private void displayEditView(boolean isUpdate) {
        // 展示编辑框和按钮
        namePanel.setVisible(true);
        updateButtonPanel.setVisible(true);

    }

    private void dismissEditView() {

    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(recordEditor);
    }

}
