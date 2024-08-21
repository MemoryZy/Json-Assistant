package cn.memoryzy.json.ui;

import cn.memoryzy.json.actions.child.RemoveListElementAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.models.HistoryModel;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/11
 */
public class JsonHistoryChooser extends DialogWrapper {
    private JPanel rootPanel;
    private JList<HistoryModel> showList;
    private EditorTextField showTextField;
    private JScrollPane scrollPane;
    private final Project project;
    private final ToolWindowEx toolWindow;

    public JsonHistoryChooser(Project project, ToolWindowEx toolWindow) {
        super(project);
        this.project = project;
        this.toolWindow = toolWindow;

        scrollPane.setViewportBorder(JBUI.Borders.empty());
        scrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.ALL));

        setModal(false);
        setTitle(JsonAssistantBundle.message("json.history.window.title"));
        setOKButtonText(JsonAssistantBundle.message("ok.button.text"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        showTextField = new CustomizedLanguageTextEditor(JsonLanguage.INSTANCE, project, "", true);
        showTextField.setFont(JBUI.Fonts.create("Consolas", 14));

        JsonViewerHistoryState historyState = JsonViewerHistoryState.getInstance(project);
        LimitedList<String> historyList = historyState.getHistoryList();
        List<HistoryModel> historyModels = HistoryModel.of(historyList);
        DefaultListModel<HistoryModel> defaultListModel = JBList.createDefaultListModel(historyModels);

        showList = new JBList<>(defaultListModel);
        showList.setFont(JBUI.Fonts.create("JetBrains Mono", 13));
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.addListSelectionListener(new ListSelectionListenerImpl());
        showList.setCellRenderer(new IconListCellRenderer());
        ((JBList<?>) showList).setEmptyText(JsonAssistantBundle.messageOnSystem("json.history.window.empty.text"));

        initRightMousePopupMenu();

        // 选中第一条
        if (!historyModels.isEmpty()) {
            showList.setSelectedIndex(0);
        }
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return showList;
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        actions.add(getCancelAction());
        actions.add(getHelpAction());
        return actions.toArray(new Action[0]);
    }

    @Override
    protected void doHelpAction() {
        BrowserUtil.browse(HyperLinks.OVERVIEW);
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            // 执行逻辑
            if (executeOkAction()) {
                close(OK_EXIT_CODE);
            }
        }
    }

    private boolean executeOkAction() {
        HistoryModel selectedValue = showList.getSelectedValue();
        if (selectedValue != null) {
            Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);
            if (Objects.nonNull(selectedContent)) {
                LanguageTextField languageTextField = JsonAssistantUtil.getLanguageTextFieldOnContent(selectedContent);
                Optional.ofNullable(languageTextField).ifPresent(e -> {
                    e.setText(selectedValue.getWholeContent());
                    toolWindow.show();
                });
            }
        }

        return true;
    }

    private void initRightMousePopupMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RemoveListElementAction(showList));

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


    public static class IconListCellRenderer extends ColoredListCellRenderer<HistoryModel> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends HistoryModel> list, HistoryModel value, int index, boolean selected, boolean hasFocus) {
            append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES);
            append(" " + value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(AllIcons.FileTypes.Json);
        }
    }

    public class ListSelectionListenerImpl implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            HistoryModel selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                showTextField.setText(selectedValue.getWholeContent());
            }
        }
    }
}
