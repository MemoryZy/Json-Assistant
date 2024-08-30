package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.HistoryModel;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/13
 */
public class RemoveListElementAction extends DumbAwareAction {
    private final JList<HistoryModel> list;
    private final EditorTextField showTextField;
    private final Runnable task;

    public RemoveListElementAction(JList<HistoryModel> list, EditorTextField showTextField, Runnable task) {
        super(JsonAssistantBundle.message("action.json.history.window.remove.text"), JsonAssistantBundle.messageOnSystem("action.json.history.window.remove.description"), null);
        this.list = list;
        this.showTextField = showTextField;
        this.task = task;
        registerCustomShortcutSet(CustomShortcutSet.fromString("DELETE"), list);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        int selectedIndex = list.getSelectedIndex();
        HistoryModel selectedValue = list.getSelectedValue();
        if (selectedValue == null) return;

        JsonViewerHistoryState state = JsonViewerHistoryState.getInstance(project);
        LimitedList<String> historyList = state.getHistory();
        historyList.remove(selectedValue.getIndex());

        List<HistoryModel> historyModels = HistoryModel.of(historyList);
        DefaultListModel<HistoryModel> listModel = (DefaultListModel<HistoryModel>) list.getModel();
        listModel.clear();
        listModel.addAll(historyModels);

        int size = listModel.getSize();
        if (size == 0) {
            showTextField.setText("");
            task.run();
        }

        // 选中被删除元素的前一个元素
        if (selectedIndex > 0) {
            list.setSelectedIndex(selectedIndex - 1);
        } else if (size > 0) {
            // 如果还有元素，选中第一个元素
            list.setSelectedIndex(0);
        }
    }
}
