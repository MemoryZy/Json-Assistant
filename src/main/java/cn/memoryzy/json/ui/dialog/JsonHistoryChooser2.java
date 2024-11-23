package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.memoryzy.json.action.structure.CollapseAllAction;
import cn.memoryzy.json.action.structure.ExpandAllAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.HistoryGroupTreeNodeType;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.HistoryEntry;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.component.PupopMenuMouseAdapter;
import cn.memoryzy.json.ui.component.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.ui.component.node.HistoryGroupTreeNode;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2024/8/22
 */
@SuppressWarnings("DuplicatedCode")
public class JsonHistoryChooser2 extends DialogWrapper {

    private Tree tree;
    private EditorTextField showTextField;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryChooser2(@Nullable Project project, ToolWindowEx toolWindow) {
        super(project, true);
        this.project = project;
        this.toolWindow = toolWindow;

        setTitle(JsonAssistantBundle.messageOnSystem("dialog.history.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.ok"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("dialog.history.cancel"));
        disabledOkAction();
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.JSON5, project, "", true);
        showTextField.setFont(UIManager.consolasFont(14));
        // 通知创建Editor
        showTextField.addNotify();

        tree = new Tree(buildTreeModel());
        tree.setDragEnabled(true);
        tree.setExpandableItemsEnabled(true);
        tree.setRootVisible(false);
        tree.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
        tree.setFont(UIManager.jetBrainsMonoFont(13));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new StyleTreeCellRenderer());
        tree.addTreeSelectionListener(new UpdateEditorTreeSelectionListener());
        tree.addMouseListener(new PupopMenuMouseAdapter(tree, buildRightMousePopupMenu()));

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree)
                .addExtraAction(new ExpandAllAction(tree, this.getRootPane(), false))
                .addExtraAction(new CollapseAllAction(tree, this.getRootPane(), false));

        // 初始化鼠标左键双击事件
        initLeftMouseDoubleClickListener();
        // 初始化回车事件
        initEnterListener();

        UIManager.updateComponentColorsScheme(tree);
        UIManager.updateComponentColorsScheme(showTextField);

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(decorator.createPanel());
        borderLayoutPanel.setBorder(JBUI.Borders.empty(3));

        JBSplitter splitter = new JBSplitter(true, 0.4f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(showTextField);
        splitter.setPreferredSize(JBUI.size(500, 500));

        return splitter;
    }

    private TreeModel buildTreeModel() {
        HistoryGroupTreeNode rootNode = new HistoryGroupTreeNode();
        JsonHistoryPersistentState persistentState = JsonHistoryPersistentState.getInstance(project);
        List<HistoryEntry> historyEntries = HistoryEntry.of(persistentState.getHistory());

        Map<String, List<HistoryEntry>> historyGroup = historyEntries.stream().collect(Collectors.groupingBy(el -> {
            String formatted = LocalDateTimeUtil.format(el.getInsertTime(), DatePattern.NORM_DATE_FORMATTER);
            return formatted != null ? formatted : PluginConstant.UNKNOWN;
        }));

        for (Map.Entry<String, List<HistoryEntry>> entry : historyGroup.entrySet()) {
            String key = entry.getKey();
            List<HistoryEntry> value = entry.getValue();

            // Map第一层是组节点
            HistoryGroupTreeNode groupNode = new HistoryGroupTreeNode(null, key, value.size(), HistoryGroupTreeNodeType.GROUP);

            // 添加底层数据节点
            for (HistoryEntry historyEntry : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryGroupTreeNode(historyEntry, null, null, HistoryGroupTreeNodeType.NODE));
            }

            rootNode.add(groupNode);
        }

        return new DefaultTreeModel(rootNode);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return tree;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.DEFAULT.getId();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            executeOkAction();
            close(OK_EXIT_CODE);
        }
    }

    private void executeOkAction() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            HistoryGroupTreeNode treeNode = (HistoryGroupTreeNode) selectionPath.getLastPathComponent();
            if (HistoryGroupTreeNodeType.NODE.equals(treeNode.getNodeType())) {
                HistoryEntry entry = treeNode.getValue();
                Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
                if (Objects.nonNull(selectedContent)) {
                    EditorEx editor = ToolWindowUtil.getEditorOnContent(selectedContent);
                    if (Objects.nonNull(editor)) {
                        WriteCommandAction.runWriteCommandAction(project, () -> PlatformUtil.setDocumentText(editor.getDocument(), entry.getLongText()));
                        toolWindow.show();
                    }
                }
            }
        }
    }


    private void initEnterListener() {
        DumbAwareAction.create(event -> doOKAction())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), tree, getDisposable());
    }

    private void initLeftMouseDoubleClickListener() {
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                doOKAction();
                return true;
            }
        }.installOn(tree);
    }


    private JPopupMenu buildRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RemoveNodeAction());

        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        return actionPopupMenu.getComponent();
    }

    public void disabledOkAction() {
        getOKAction().setEnabled(false);
    }

    public void enabledOkAction() {
        getOKAction().setEnabled(true);
    }

    public class RemoveNodeAction extends DumbAwareAction {

        public RemoveNodeAction() {
            super(JsonAssistantBundle.message("action.structure.remove.text"), JsonAssistantBundle.messageOnSystem("action.structure.remove.description"), null);
            registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), tree);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) return;

            // 刷新Tree
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent != null) {
                    // 先获取父节点的子节点数量，如果只有这一个，那就连父节点一起删除
                    ((DefaultMutableTreeNode) node.getParent()).remove(node);
                    if (parent.getChildCount() == 1) {
                        ((DefaultMutableTreeNode) parent.getParent()).remove(parent);
                    }

                    ((DefaultTreeModel) tree.getModel()).reload();
                    // TODO 如果在reload前节点（其他）是展开的，那么保持

                }
            }


            // int selectedIndex = showList.getSelectedIndex();
            // HistoryEntry selectedValue = showList.getSelectedValue();
            // if (selectedValue == null) return;
            //
            // JsonHistoryPersistentState state = JsonHistoryPersistentState.getInstance(project);
            // LimitedList historyList = state.getHistory();
            // historyList.remove(selectedValue.getIndex());
            //
            // List<HistoryEntry> historyEntries = HistoryEntry.of(historyList);
            // NameFilteringListModel<HistoryEntry> listModel = (NameFilteringListModel<HistoryEntry>) showList.getModel();
            // listModel.replaceAll(historyEntries);
            //
            // int size = listModel.getSize();
            // if (size == 0) {
            //     showTextField.setText("");
            //     disabledOkAction();
            // }
            //
            // // 选中被删除元素的前一个元素
            // if (selectedIndex > 0) {
            //     showList.setSelectedIndex(selectedIndex - 1);
            // } else if (size > 0) {
            //     // 如果还有元素，选中第一个元素
            //     showList.setSelectedIndex(0);
            // }
            //
            // rebuildListWithFilter();
        }
    }


    public static class StyleTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            HistoryGroupTreeNode treeNode = (HistoryGroupTreeNode) value;
            HistoryGroupTreeNodeType nodeType = treeNode.getNodeType();

            if (HistoryGroupTreeNodeType.NODE.equals(nodeType)) {
                setIcon(AllIcons.FileTypes.Json);
                append(treeNode.toString());
            } else {
                append(treeNode + " (" + treeNode.getSize() + ")");
            }

            SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
        }
    }


    public class UpdateEditorTreeSelectionListener implements TreeSelectionListener {
        private int lastLineCount = 0;

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                HistoryGroupTreeNode treeNode = (HistoryGroupTreeNode) Objects.requireNonNull(selectionPath).getLastPathComponent();
                if (HistoryGroupTreeNodeType.GROUP.equals(treeNode.getNodeType())) {
                    showTextField.setText("");
                } else {
                    showTextField.setText(JsonAssistantUtil.normalizeLineEndings(treeNode.getValue().getLongText()));
                }

                // -------------- 重新绘制
                Document document = showTextField.getDocument();
                int newLineCount = document.getLineCount();
                if (lastLineCount != newLineCount) {
                    lastLineCount = newLineCount;
                    Editor editor = showTextField.getEditor();
                    UIManager.repaintEditor(Objects.requireNonNull(editor));
                }
            }
        }
    }

}
