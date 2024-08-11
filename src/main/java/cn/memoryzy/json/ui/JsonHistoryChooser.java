package cn.memoryzy.json.ui;

import cn.memoryzy.json.model.HistoryModel;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.basic.CustomizedLanguageTextEditor;
import com.intellij.icons.AllIcons;
import com.intellij.json.json5.Json5Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
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

    public JsonHistoryChooser(Project project) {
        super(project);
        this.project = project;

        setModal(false);
        // setTitle(JsonAssistantBundle.message("json.structure.window.title"));
        // setOKButtonText(JsonAssistantBundle.message("ok.button.text"));
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
        showList.setCellRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends HistoryModel> list, HistoryModel value, int index, boolean selected, boolean hasFocus) {
                append((index + 1) + "  ", SimpleTextAttributes.GRAY_ATTRIBUTES);
                append(" " + value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                setIcon(AllIcons.FileTypes.Json);
            }
        });

        // 选中第一条
        if (!historyModels.isEmpty()) {
            showList.setSelectedIndex(0);
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
