package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.structure.CollapseAllAction;
import cn.memoryzy.json.action.structure.ExpandAllAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.ui.listener.TreeRightClickPopupMenuMouseAdapter;
import cn.memoryzy.json.ui.node.HistoryTreeNode;
import cn.memoryzy.json.ui.panel.CombineCardLayout;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
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
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
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
public class JsonHistoryTreeChooser extends DialogWrapper {

    private Tree tree;
    private EditorTextField showTextField;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryTreeChooser(@Nullable Project project, ToolWindowEx toolWindow) {
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
        tree.addMouseListener(new TreeRightClickPopupMenuMouseAdapter(tree, buildRightMousePopupMenu()));

        // 如果只有一个组节点，那么默认将其展开
        expandSingleNode();

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
                JsonEntry entry = treeNode.getValue();
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

    private TreeModel buildTreeModel() {
        return new DefaultTreeModel(buildRootNode(JsonHistoryPersistentState.getInstance(project).getHistory()));
    }

    private TreeNode buildRootNode(HistoryLimitedList historyList) {
        HistoryTreeNode rootNode = new HistoryTreeNode();
        Map<String, List<JsonEntry>> historyGroup = historyList.stream().collect(Collectors.groupingBy(el -> {
            String formatted = LocalDateTimeUtil.format(el.getInsertTime(), DatePattern.NORM_DATE_FORMATTER);
            return formatted != null ? formatted : PluginConstant.UNKNOWN;
        }));

        List<Map.Entry<String, List<JsonEntry>>> entryList = historyGroup.entrySet().stream()
                .sorted(Comparator.comparing(el ->
                        PluginConstant.UNKNOWN.equals(el.getKey())
                                ? LocalDate.MIN
                                : LocalDate.parse(el.getKey(), DatePattern.NORM_DATE_FORMATTER)))
                .collect(Collectors.toList());

        Collections.reverse(entryList);
        for (Map.Entry<String, List<JsonEntry>> entry : entryList) {
            String key = entry.getKey();
            List<JsonEntry> value = entry.getValue();

            // 排序List
            value.sort(Comparator.comparing(JsonEntry::getInsertTime).reversed());

            // Map第一层是组节点
            HistoryTreeNode groupNode = new HistoryTreeNode(null, key, value.size(), HistoryTreeNodeType.GROUP);

            // 添加底层数据节点
            for (JsonEntry historyEntry : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryTreeNode(historyEntry, null, null, HistoryTreeNodeType.NODE));
            }

            rootNode.add(groupNode);
        }

        return rootNode;
    }

    private void expandSingleNode() {
        HistoryTreeNode rootNode = (HistoryTreeNode) tree.getModel().getRoot();
        List<TreeNode> children = JsonAssistantUtil.enumerationToList(rootNode.children());
        // 若只有一个节点
        if (children.size() == 1) {
            HistoryTreeNode node = (HistoryTreeNode) children.get(0);
            // 展开
            tree.expandPath(new TreePath(node.getPath()));
            // 选中该节点下的第一个元素
            List<TreeNode> nodeList = JsonAssistantUtil.enumerationToList(node.children());
            // 第一个节点元素
            HistoryTreeNode child = (HistoryTreeNode) nodeList.get(0);
            // 转为树路径
            TreePath path = new TreePath(child.getPath());
            // 选中节点
            tree.setSelectionPath(path);
        }
    }


    private JPopupMenu buildRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new SetNameAction());
        group.add(Separator.create());
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


    class SetNameAction extends DumbAwareAction implements UpdateInBackground {

        public SetNameAction() {
            super(JsonAssistantBundle.message("action.structure.setName.text"), JsonAssistantBundle.messageOnSystem("action.structure.setName.description"), null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                HistoryTreeNode node = (HistoryTreeNode) selectionPath.getLastPathComponent();
                if (HistoryTreeNodeType.NODE == node.getNodeType()) {
                    JsonEntry entry = node.getValue();
                    String name = entry.getName();
                    String newName = Messages.showInputDialog(
                            project,
                            null,
                            JsonAssistantBundle.messageOnSystem("dialog.assign.history.name.title"),
                            null,
                            name,
                            new NameValidator(JsonHistoryPersistentState.getInstance(project).getHistory()));
                    if (StrUtil.isNotBlank(newName)) {
                        entry.setName(newName);
                        UIManager.repaintComponent(tree);
                    }
                }
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean enabled = false;
            Presentation presentation = e.getPresentation();
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                HistoryTreeNode node = (HistoryTreeNode) selectionPath.getLastPathComponent();
                if (HistoryTreeNodeType.NODE == node.getNodeType()) {
                    enabled = true;
                    JsonEntry value = node.getValue();
                    String name = value.getName();
                    if (StrUtil.isNotBlank(name)) {
                        presentation.setText(JsonAssistantBundle.message("action.structure.rename.text"));
                        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.structure.rename.description"));
                    }
                }
            }

            presentation.setEnabledAndVisible(enabled);
        }
    }


    class RemoveNodeAction extends DumbAwareAction implements UpdateInBackground {

        public RemoveNodeAction() {
            super(JsonAssistantBundle.message("action.structure.remove.text"), JsonAssistantBundle.messageOnSystem("action.structure.remove.description"), null);
            registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), tree);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            Project project = event.getProject();

            // 刷新Tree
            TreePath selectionPath = tree.getSelectionPath();
            // 记录树节点展开状态
            Map<TreePath, Boolean> expandedStates = UIManager.recordExpandedStates(tree);

            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            HistoryTreeNode oriRootNode = (HistoryTreeNode) model.getRoot();

            HistoryTreeNode node = (HistoryTreeNode) Objects.requireNonNull(selectionPath).getLastPathComponent();
            HistoryTreeNode parent = (HistoryTreeNode) node.getParent();
            final String parentName = parent.toString();
            final int nodeIndex = parent.getIndex(node);

            List<Integer> deletionList = new ArrayList<>();
            boolean isNodeDeletion = appendDeletionListAndNodeType(node, deletionList);

            // 如果删除的节点只有它一个元素，那么则删除父节点，并将焦点切换到旁边的节点
            int groupIndex = -1;
            if (isNodeDeletion) {
                groupIndex = oriRootNode.getIndex(parent);
            }

            // 获取历史记录列表
            HistoryLimitedList historyList = JsonHistoryPersistentState.getInstance(Objects.requireNonNull(project)).getHistory();
            // 删除真实数据
            historyList.removeById(deletionList.toArray(new Integer[0]));

            // 重新构建树节点
            TreeNode newRootNode = buildRootNode(historyList);
            model.setRoot(newRootNode);

            // 当不存在数据了，清除编辑框文本，禁用按钮
            if (newRootNode.getChildCount() == 0) {
                showTextField.setText("");
                disabledOkAction();
            } else {
                chooseNextNodeAfterRemoval(isNodeDeletion, newRootNode, parentName, nodeIndex, groupIndex);
            }

            restoreExpandedStates(expandedStates, newRootNode);
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setEnabled(Objects.nonNull(getEventProject(event)) && Objects.nonNull(tree.getSelectionPath()));
        }

        /**
         * 补充将要删除的Id列表，并且返回删除的是否为节点类型
         *
         * @param node         选中节点
         * @param deletionList 将要删除的Id列表
         * @return 是否为节点类型，true为节点类型，否则为组类型
         */
        private boolean appendDeletionListAndNodeType(HistoryTreeNode node, List<Integer> deletionList) {
            boolean isNodeDeletion = false;
            // 如果类型是组，那么删除组节点及以下的数据，如果类型是具体数据，那么删除具体数据节点
            if (HistoryTreeNodeType.GROUP.equals(node.getNodeType())) {
                Enumeration<TreeNode> children = node.children();
                while (children.hasMoreElements()) {
                    HistoryTreeNode treeNode = (HistoryTreeNode) children.nextElement();
                    deletionList.add(treeNode.getValue().getId());
                }
            } else {
                isNodeDeletion = true;
                deletionList.add(node.getValue().getId());
            }

            return isNodeDeletion;
        }

        private void chooseNextNodeAfterRemoval(boolean isNodeDeletion, TreeNode newRootNode, String parentName, int nodeIndex, int groupIndex) {
            HistoryTreeNode child;
            int selectIndex = (nodeIndex > 0) ? nodeIndex - 1 : 0;
            // 如果是删除节点，那就计算索引
            if (isNodeDeletion) {
                // 1.1 根据名称匹配现在的组
                TreeNode parentNode = null;
                Enumeration<? extends TreeNode> children = newRootNode.children();
                while (children.hasMoreElements()) {
                    TreeNode treeNode = children.nextElement();
                    if (Objects.equals(treeNode.toString(), parentName)) {
                        parentNode = treeNode;
                        break;
                    }
                }

                //  1.删除的是最后一个节点，整个组都删除，那就将焦点切换到旁边组节点
                if (parentNode == null) {
                    // 因为当前选中的是节点类型，所以先获取父节点（组），再计算组在root内的索引
                    int groupSelectIndex = (groupIndex > 0) ? groupIndex - 1 : 0;
                    // 获取旁边的组节点
                    HistoryTreeNode childGroup = (HistoryTreeNode) newRootNode.getChildAt(groupSelectIndex);
                    // 获取节点的路径
                    TreePath path = new TreePath(childGroup.getPath());
                    // 选中节点
                    tree.setSelectionPath(path);
                    // 滚动到选中的节点
                    tree.scrollPathToVisible(path);
                    return;
                }

                //  2.删除的是组内的任一节点，那么就计算索引，然后选中旁边的节点
                //  ? 选中被删除元素的前一个元素 : 选中第一个元素
                child = (HistoryTreeNode) parentNode.getChildAt(selectIndex);
            } else {
                // 如果是删除组，那就找到旁边节点（也根据索引），选中并展开
                child = (HistoryTreeNode) newRootNode.getChildAt(selectIndex);
            }

            // 获取节点的路径
            TreePath path = new TreePath(child.getPath());
            // 选中节点
            tree.setSelectionPath(path);
            // 滚动到选中的节点
            tree.scrollPathToVisible(path);
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
                    if (groupName == null) {
                        continue;
                    }

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
                        tree.expandPath(path);
                    }
                }
            }
        }
    }


    static class StyleTreeCellRenderer extends ColoredTreeCellRenderer {
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


    class UpdateEditorTreeSelectionListener implements TreeSelectionListener {
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
                    // 只有处于编辑器页面，才能开启ok按钮
                    Content content = ToolWindowUtil.getSelectedContent(toolWindow);
                    Boolean editorCardDisplayed = Optional.ofNullable(ToolWindowUtil.getPanelOnContent(content))
                            .map(JsonAssistantToolWindowPanel::getCardLayout)
                            .map(CombineCardLayout::isEditorCardDisplayed)
                            .orElse(false);

                    if (editorCardDisplayed) {
                        enabledOkAction();
                    }
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

    public static class NameValidator implements InputValidator {
        private final HistoryLimitedList historyList;

        public NameValidator(HistoryLimitedList historyList) {
            this.historyList = historyList;
        }

        @Override
        public boolean checkInput(String inputString) {
            return StrUtil.isNotBlank(inputString)
                    && inputString.length() <= 50
                    && historyList.stream().noneMatch(element -> inputString.equals(element.getName()));
        }

        @Override
        public boolean canClose(String inputString) {
            return true;
        }
    }

}
