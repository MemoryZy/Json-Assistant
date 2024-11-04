package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.enums.UrlEnum;
import cn.memoryzy.json.model.HistoryModel;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.component.editor.ViewerModeLanguageTextEditor;
import cn.memoryzy.json.util.ToolWindowUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/22
 */
@SuppressWarnings("DuplicatedCode")
public class JsonHistoryChooser extends DialogWrapper {

    private JList<HistoryModel> showList;
    private EditorTextField showTextField;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryChooser(@Nullable Project project, ToolWindowEx toolWindow) {
        super(project, true);
        this.project = project;
        this.toolWindow = toolWindow;

        setTitle(JsonAssistantBundle.messageOnSystem("json.history.window.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("json.history.window.ok.button.text"));
        setCancelButtonText(JsonAssistantBundle.messageOnSystem("json.history.window.cancel.button.text"));
        disabledOkAction();
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        showTextField = new ViewerModeLanguageTextEditor(LanguageHolder.JSON, project, "", true);
        showTextField.setFont(UIManager.consolasFont(14));

        showList = new JBList<>(fillHistoryListModel());
        showList.setFont(UIManager.jetBrainsMonoFont(13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.addListSelectionListener(new UpdateEditorListSelectionListener());
        showList.setCellRenderer(new IconListCellRenderer());
        ((JBList<?>) showList).setEmptyText(JsonAssistantBundle.messageOnSystem("json.history.window.empty.text"));

        // 初始化右键弹出菜单
        initRightMousePopupMenu();
        // 初始化鼠标左键双击事件
        initLeftMouseDoubleClickListener();
        // 初始化回车事件
        initEnterListener();
        // 默认选中第一条
        selectFirstItemInList();

        UIManager.updateListColorsScheme(showList);
        UIManager.updateEditorTextFieldColorsScheme(showTextField);

        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.addToCenter(UIManager.wrapListWithFilter(showList, HistoryModel::getShortText, true));
        borderLayoutPanel.setBorder(JBUI.Borders.empty(3));
        rebuildListWithFilter();

        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(borderLayoutPanel);
        splitter.setSecondComponent(showTextField);

        ScrollingUtil.installActions(showList);
        ScrollingUtil.ensureSelectionExists(showList);

        splitter.setPreferredSize(JBUI.size(500, 500));

        return splitter;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showList;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlEnum.DEFAULT.getId();
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
        HistoryModel selectedValue = showList.getSelectedValue();
        if (selectedValue != null) {
            Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
            if (Objects.nonNull(selectedContent)) {
                EditorEx editor = ToolWindowUtil.getEditorOnContent(selectedContent);
                if (Objects.nonNull(editor)) {
                    WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(selectedValue.getLongText()));
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

    private void initEnterListener() {
        DumbAwareAction.create(event -> doOKAction())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), showList, getDisposable());
    }

    private void initLeftMouseDoubleClickListener() {
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                doOKAction();
                return true;
            }
        }.installOn(showList);
    }

    private DefaultListModel<HistoryModel> fillHistoryListModel() {
        JsonHistoryPersistentState historyState = JsonHistoryPersistentState.getInstance(project);
        LimitedList historyList = historyState.getHistory();
        List<HistoryModel> historyModels = HistoryModel.of(historyList);
        return JBList.createDefaultListModel(historyModels);
    }

    private void selectFirstItemInList() {
        // 选中第一条
        ListModel<HistoryModel> listModel = showList.getModel();
        if (listModel.getSize() > 0) {
            showList.setSelectedIndex(0);
            enabledOkAction();
        }
    }

    private void initRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RemoveListElementAction());

        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group);
        JPopupMenu popupMenu = actionPopupMenu.getComponent();

        showList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int x = e.getX();
                    int y = e.getY();

                    HistoryModel selectedValue = showList.getSelectedValue();
                    if (Objects.isNull(selectedValue)) {
                        int index = showList.locationToIndex(new Point(x, y));
                        if (index != -1) {
                            showList.setSelectedIndex(index);
                            popupMenu.show(showList, x, y);
                        }
                    } else {
                        popupMenu.show(showList, x, y);
                    }
                }
            }
        });
    }

    public void disabledOkAction() {
        getOKAction().setEnabled(false);
    }

    public void enabledOkAction() {
        getOKAction().setEnabled(true);
    }

    public class RemoveListElementAction extends DumbAwareAction {

        public RemoveListElementAction() {
            super(JsonAssistantBundle.message("action.json.history.window.remove.text"), JsonAssistantBundle.messageOnSystem("action.json.history.window.remove.description"), null);
            registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), showList);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) return;
            int selectedIndex = showList.getSelectedIndex();
            HistoryModel selectedValue = showList.getSelectedValue();
            if (selectedValue == null) return;

            JsonHistoryPersistentState state = JsonHistoryPersistentState.getInstance(project);
            LimitedList historyList = state.getHistory();
            historyList.remove(selectedValue.getIndex());

            List<HistoryModel> historyModels = HistoryModel.of(historyList);
            NameFilteringListModel<HistoryModel> listModel = (NameFilteringListModel<HistoryModel>) showList.getModel();
            listModel.replaceAll(historyModels);

            int size = listModel.getSize();
            if (size == 0) {
                showTextField.setText("");
                disabledOkAction();
            }

            // 选中被删除元素的前一个元素
            if (selectedIndex > 0) {
                showList.setSelectedIndex(selectedIndex - 1);
            } else if (size > 0) {
                // 如果还有元素，选中第一个元素
                showList.setSelectedIndex(0);
            }

            rebuildListWithFilter();
        }
    }

    public static class IconListCellRenderer extends ColoredListCellRenderer<HistoryModel> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends HistoryModel> list, HistoryModel value, int index, boolean selected, boolean hasFocus) {
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES, false);
            append(" " + value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
            setIcon(AllIcons.FileTypes.Json);
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
        }
    }

    public class UpdateEditorListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            HistoryModel selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                showTextField.setText(selectedValue.getLongText());
            }
        }
    }
}
