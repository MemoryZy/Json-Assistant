package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.HistoryEntry;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.ui.listener.ListRightClickPopupMenuMouseAdapter;
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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.speedSearch.ListWithFilter;
import com.intellij.ui.speedSearch.NameFilteringListModel;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/22
 */
@SuppressWarnings("DuplicatedCode")
public class JsonHistoryListChooser extends DialogWrapper {

    private JBList<HistoryEntry> showList;
    private EditorTextField showTextField;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryListChooser(@Nullable Project project, ToolWindowEx toolWindow) {
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

        showList = new JBList<>(fillHistoryListModel());
        showList.setFont(UIManager.jetBrainsMonoFont(13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.addListSelectionListener(new UpdateEditorListSelectionListener());
        showList.setCellRenderer(new StyleListCellRenderer());
        showList.setEmptyText(JsonAssistantBundle.messageOnSystem("dialog.history.empty.text"));
        showList.addMouseListener(new ListRightClickPopupMenuMouseAdapter(showList, buildRightMousePopupMenu()));

        // 初始化鼠标左键双击事件
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                doOKAction();
                return true;
            }
        }.installOn(showList);

        // 初始化回车事件
        DumbAwareAction.create(event -> doOKAction())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), showList, getDisposable());

        // 默认选中第一条
        selectFirstItemInList();

        UIManager.updateComponentColorsScheme(showList);
        UIManager.updateComponentColorsScheme(showTextField);

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(UIManager.wrapListWithFilter(showList, HistoryEntry::getShortText, true));
        borderLayoutPanel.setBorder(JBUI.Borders.empty(3));
        rebuildListWithFilter();

        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(showTextField);

        ScrollingUtil.installActions(showList);
        ScrollingUtil.ensureSelectionExists(showList);

        splitter.setPreferredSize(JBUI.size(550, 570));

        return splitter;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showList;
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
        HistoryEntry selectedValue = showList.getSelectedValue();
        if (selectedValue != null) {
            Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
            if (Objects.nonNull(selectedContent)) {
                EditorEx editor = ToolWindowUtil.getEditorOnContent(selectedContent);
                if (Objects.nonNull(editor)) {
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            PlatformUtil.setDocumentText(editor.getDocument(), selectedValue.getJsonString()));

                    toolWindow.show();
                }
            }
        }
    }

    private void rebuildListWithFilter() {
        ListWithFilter<?> listWithFilter = ComponentUtil.getParentOfType(ListWithFilter.class, showList);
        if (listWithFilter != null) {
            listWithFilter.getSpeedSearch().update();
            if (showList.getModel().getSize() == 0) listWithFilter.resetFilter();
        }
    }

    private DefaultListModel<HistoryEntry> fillHistoryListModel() {
        HistoryLimitedList history = JsonHistoryPersistentState.getInstance(project).getHistory();
        return JBList.createDefaultListModel(history);
    }

    private void selectFirstItemInList() {
        // 选中第一条
        ListModel<HistoryEntry> listModel = showList.getModel();
        if (listModel.getSize() > 0) {
            showList.setSelectedIndex(0);
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
    }

    private JPopupMenu buildRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new SetNameAction());
        group.add(Separator.create());
        group.add(new RemoveElementAction());

        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        return actionPopupMenu.getComponent();
    }

    public void disabledOkAction() {
        getOKAction().setEnabled(false);
    }

    public void enabledOkAction() {
        getOKAction().setEnabled(true);
    }


    class SetNameAction extends DumbAwareAction {

        public SetNameAction() {
            super(JsonAssistantBundle.message("action.structure.setName.text"), JsonAssistantBundle.messageOnSystem("action.structure.setName.description"), null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            HistoryEntry selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                String name = selectedValue.getName();
                String newName = Messages.showInputDialog(project, null, "指定记录名称", null, name, new JsonHistoryTreeChooser.NameValidator());
                if (StrUtil.isNotBlank(newName)) {
                    selectedValue.setName(newName);
                    UIManager.repaintComponent(showList);
                }
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            boolean enabled = false;
            Presentation presentation = e.getPresentation();
            HistoryEntry selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                enabled = true;
                String name = selectedValue.getName();
                if (StrUtil.isNotBlank(name)) {
                    presentation.setText(JsonAssistantBundle.message("action.structure.rename.text"));
                    presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.structure.rename.description"));
                }
            }

            presentation.setEnabledAndVisible(enabled);
        }
    }

    class RemoveElementAction extends DumbAwareAction {

        public RemoveElementAction() {
            super(JsonAssistantBundle.message("action.history.remove.text"), JsonAssistantBundle.messageOnSystem("action.history.remove.description"), null);
            registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), showList);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            Project project = event.getProject();
            int selectedIndex = showList.getSelectedIndex();
            HistoryEntry selectedValue = showList.getSelectedValue();

            // 删除真实数据
            HistoryLimitedList history = JsonHistoryPersistentState.getInstance(Objects.requireNonNull(project)).getHistory();
            history.removeById(selectedValue.getId());

            // 替换List数据为最新的
            NameFilteringListModel<HistoryEntry> listModel = (NameFilteringListModel<HistoryEntry>) showList.getModel();
            listModel.replaceAll(history);

            // 若没有数据，则置空
            int size = listModel.getSize();
            if (size == 0) {
                showTextField.setText("");
                disabledOkAction();
            } else {
                // 选中被删除元素的前一个元素
                if (selectedIndex > 0) {
                    showList.setSelectedIndex(selectedIndex - 1);
                } else {
                    // 如果还有元素，选中第一个元素
                    showList.setSelectedIndex(0);
                }
            }

            rebuildListWithFilter();
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setEnabled(Objects.nonNull(getEventProject(event)) && Objects.nonNull(showList.getSelectedValue()));
        }
    }

    static class StyleListCellRenderer extends ColoredListCellRenderer<HistoryEntry> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends HistoryEntry> list, HistoryEntry value, int index, boolean selected, boolean hasFocus) {
            String name = value.getName();
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
            append(" " + (StrUtil.isNotBlank(name) ? name : value.getShortText()), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            setIcon(AllIcons.FileTypes.Json);
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
        }
    }

    class UpdateEditorListSelectionListener implements ListSelectionListener {
        private int lastLineCount = 0;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            HistoryEntry selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                showTextField.setText(JsonAssistantUtil.normalizeLineEndings(selectedValue.getJsonString()));

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

