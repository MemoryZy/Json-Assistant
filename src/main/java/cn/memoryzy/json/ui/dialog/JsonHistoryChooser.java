package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.memoryzy.json.action.structure.CollapseAllAction;
import cn.memoryzy.json.action.structure.ExpandAllAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.HistoryEntry;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.component.PupopMenuMouseAdapter;
import cn.memoryzy.json.ui.component.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.ui.component.node.HistoryTreeNode;
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
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2024/8/22
 */
@SuppressWarnings("DuplicatedCode")
public class JsonHistoryChooser extends DialogWrapper {

    private Tree tree;
    private EditorTextField showTextField;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryChooser(@Nullable Project project, ToolWindowEx toolWindow) {
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

        // 初始化回车事件
        DumbAwareAction.create(event -> doOKAction())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), tree, getDisposable());

        // 初始化鼠标左键双击事件
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                doOKAction();
                return true;
            }
        }.installOn(tree);

        UIManager.updateComponentColorsScheme(showTextField);

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(decorator.createPanel());
        borderLayoutPanel.setBorder(JBUI.Borders.empty(3));

        JBSplitter splitter = new JBSplitter(true, 0.4f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(showTextField);
        splitter.setPreferredSize(JBUI.size(550, 570));

        return splitter;
    }

    private TreeModel buildTreeModel() {
        return new DefaultTreeModel(buildRootNode(JsonHistoryPersistentState.getInstance(project).getHistory()));
    }

    private TreeNode buildRootNode(HistoryLimitedList historyList) {
        HistoryTreeNode rootNode = new HistoryTreeNode();
        Map<String, List<HistoryEntry>> historyGroup = historyList.stream().collect(Collectors.groupingBy(el -> {
            String formatted = LocalDateTimeUtil.format(el.getInsertTime(), DatePattern.NORM_DATE_FORMATTER);
            return formatted != null ? formatted : PluginConstant.UNKNOWN;
        }));

        List<Map.Entry<String, List<HistoryEntry>>> entryList = historyGroup.entrySet().stream()
                .sorted(Comparator.comparing(el ->
                        PluginConstant.UNKNOWN.equals(el.getKey())
                                ? LocalDate.MIN
                                : LocalDate.parse(el.getKey(), DatePattern.NORM_DATE_FORMATTER)))
                .collect(Collectors.toList());

        Collections.reverse(entryList);
        for (Map.Entry<String, List<HistoryEntry>> entry : entryList) {
            String key = entry.getKey();
            List<HistoryEntry> value = entry.getValue();

            // 排序List
            value.sort(Comparator.comparing(HistoryEntry::getInsertTime));

            // Map第一层是组节点
            HistoryTreeNode groupNode = new HistoryTreeNode(null, key, value.size(), HistoryTreeNodeType.GROUP);

            // 添加底层数据节点
            for (HistoryEntry historyEntry : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryTreeNode(historyEntry, null, null, HistoryTreeNodeType.NODE));
            }

            rootNode.add(groupNode);
        }

        return rootNode;
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
            HistoryTreeNode treeNode = (HistoryTreeNode) selectionPath.getLastPathComponent();
            if (HistoryTreeNodeType.NODE.equals(treeNode.getNodeType())) {
                HistoryEntry entry = treeNode.getValue();
                Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
                if (Objects.nonNull(selectedContent)) {
                    EditorEx editor = ToolWindowUtil.getEditorOnContent(selectedContent);
                    if (Objects.nonNull(editor)) {
                        WriteCommandAction.runWriteCommandAction(project, () -> PlatformUtil.setDocumentText(editor.getDocument(), entry.getJsonString()));
                        toolWindow.show();
                    }
                }
            }
        }
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
                // 获取将要删除的历史记录Id列表
                List<Integer> deletionList = getDeletionList(selectionPath);
                // 获取历史记录列表
                HistoryLimitedList historyList = JsonHistoryPersistentState.getInstance(project).getHistory();
                // 删除真实数据
                historyList.removeById(deletionList.toArray(new Integer[0]));

                // 重新构建树节点
                TreeNode rootNode = buildRootNode(historyList);
                ((DefaultTreeModel) tree.getModel()).setRoot(rootNode);
                UIManager.expandSecondaryNode(tree, rootNode);

                // 当不存在数据了，清除编辑框文本，禁用按钮
                if (rootNode.getChildCount() == 0) {
                    showTextField.setText("");
                    disabledOkAction();
                }
            }
        }

        /**
         * 获取删除列表（Id）
         *
         * @param selectionPath 选择的路径
         * @return 将要删除的历史记录Id列表
         */
        @NotNull
        private List<Integer> getDeletionList(TreePath selectionPath) {
            HistoryTreeNode node = (HistoryTreeNode) selectionPath.getLastPathComponent();

            // 如果类型是组，那么删除组节点及以下的数据，如果类型是具体数据，那么删除具体数据节点
            List<Integer> deletionList = new ArrayList<>();
            if (HistoryTreeNodeType.GROUP.equals(node.getNodeType())) {
                Enumeration<TreeNode> children = node.children();
                while (children.hasMoreElements()) {
                    HistoryTreeNode treeNode = (HistoryTreeNode) children.nextElement();
                    deletionList.add(treeNode.getValue().getId());
                }
            } else {
                deletionList.add(node.getValue().getId());
            }

            return deletionList;
        }
    }


    public static class StyleTreeCellRenderer extends ColoredTreeCellRenderer {
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

            SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
        }
    }


    public class UpdateEditorTreeSelectionListener implements TreeSelectionListener {
        private int lastLineCount = 0;

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                HistoryTreeNode treeNode = (HistoryTreeNode) Objects.requireNonNull(selectionPath).getLastPathComponent();
                if (HistoryTreeNodeType.GROUP.equals(treeNode.getNodeType())) {
                    showTextField.setText("");
                    disabledOkAction();
                } else {
                    showTextField.setText(JsonAssistantUtil.normalizeLineEndings(treeNode.getValue().getJsonString()));
                    enabledOkAction();
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
