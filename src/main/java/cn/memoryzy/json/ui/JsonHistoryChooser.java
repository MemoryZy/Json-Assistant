package cn.memoryzy.json.ui;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.HistoryModel;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/11
 */
public class JsonHistoryChooser extends DialogWrapper {
    private JPanel rootPanel;
    private JList<HistoryModel> showList;
    private EditorTextField showTextField;
    private final Project project;
    private final JsonViewerWindow window;

    public JsonHistoryChooser(Project project, JsonViewerWindow window) {
        super(project);
        this.project = project;
        this.window = window;

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
        Font font = new Font("Consolas", Font.PLAIN, 15);
        showTextField = new CustomizedLanguageTextEditor(Json5Language.INSTANCE, project, "", true);
        showTextField.setFont(font);

        List<HistoryModel> historyModels = HistoryModel.of(JsonViewerHistoryState.getInstance(project).getHistoryList());
        DefaultListModel<HistoryModel> defaultListModel = JBList.createDefaultListModel(historyModels);

        showList = new JBList<>(defaultListModel);
        showList.setFont(font);
        showList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showList.addListSelectionListener(new ListSelectionListenerImpl());
        showList.setCellRenderer(new IconListCellRenderer());
        ((JBList<?>)showList).setEmptyText(JsonAssistantBundle.messageOnSystem("json.history.window.empty.text"));

        // 选中第一条
        if (!historyModels.isEmpty()) {
            showList.setSelectedIndex(0);
        }

        showList.requestFocusInWindow();
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
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID);
        if (toolWindow != null) {
            HistoryModel selectedValue = showList.getSelectedValue();
            if (selectedValue != null) {
                LanguageTextField jsonTextField = window.getJsonTextField();
                jsonTextField.setText(selectedValue.getWholeContent());
                toolWindow.show();
            }
        }

        return true;
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
