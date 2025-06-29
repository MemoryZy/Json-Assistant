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
import cn.memoryzy.json.ui.editor.AutoCompleteWrapper;
import cn.memoryzy.json.ui.node.HistoryTreeNode;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
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

    private final Editor recordEditor;
    private final AutoCompleteWrapper completeWrapper;
    private final JBList<JsonRecord> showList;
    private final Tree showTree;
    private final JBCardLayout cardLayout;
    private final JPanel cardPanel;

    public HistoryToolWindowComponentProvider(Project project) {
        this.project = project;
        this.historyManager = HistoryManager.getInstance(project);
        this.historyState = ToolWindowSettings.getInstance().getHistoryState();

        this.recordEditor = createJsonEditor();
        this.showList = new JBList<>(createListModel());
        this.showTree = new Tree(createTreeModel());
        this.completeWrapper = new AutoCompleteWrapper(project, getLatestVariants(), () -> PluginConstant.HISTORY_SEARCH_HISTORY_KEY);

        this.cardLayout = new JBCardLayout();
        this.cardPanel = new JPanel(cardLayout);
    }



    public JComponent createComponent() {
        JBSplitter splitter = new JBSplitter(false, 0.5f);
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
        actionGroup.add(new AddHistoryAction());
        actionGroup.add(new RemoveHistoryAction());
        actionGroup.add(new EditHistoryAction());

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
        return new BorderLayoutPanel().addToCenter(recordEditor.getComponent());
    }

    private JScrollPane createListScrollPane() {
        showList.setFont(UIUtils.jetBrainsMonoFont(JBUIScale.scaleFontSize(12)));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.setCellRenderer(new StyleListCellRenderer());
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
        return UIUtils.wrapScrollPane(showList);
    }

    private JScrollPane createTreeScrollPane() {

        return null;
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

        return null;
    }

    private TreeNode combineRootTreeNode(List<JsonRecord> recentHistories) {
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
        Editor editor = PlatformUtil.createEditor(project, "record", FileTypeHolder.JSON5, true, EditorKind.MAIN_EDITOR, "");
        editor.getSettings().setLineNumbersShown(true);
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

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(recordEditor);
    }


    static class StyleListCellRenderer extends ColoredListCellRenderer<JsonRecord> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends JsonRecord> list, JsonRecord value, int index, boolean selected, boolean hasFocus) {
            String name = value.getName();
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
            append( (StrUtil.isNotBlank(name) ? name : value.getDisplayText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            // setIcon(AllIcons.FileTypes.Json);
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
        }
    }

}
