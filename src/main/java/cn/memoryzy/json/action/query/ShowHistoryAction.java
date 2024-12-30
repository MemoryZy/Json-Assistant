package cn.memoryzy.json.action.query;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.enums.JsonQuerySchema;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.QueryState;
import cn.memoryzy.json.ui.panel.SearchWrapper;
import cn.memoryzy.json.util.UIManager;
import com.intellij.find.FindBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class ShowHistoryAction extends DumbAwareAction {

    public static final String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";
    public static final String JMES_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JmesPathHistory";

    private final SearchWrapper searchWrapper;
    private final JComponent searchTextField;
    private final QueryState queryState;

    public ShowHistoryAction(SearchWrapper searchWrapper, JComponent searchTextField) {
        super(FindBundle.message("find.search.history"), null, AllIcons.Actions.SearchWithHistory);
        this.searchWrapper = searchWrapper;
        this.searchTextField = searchTextField;
        this.queryState = JsonAssistantPersistentState.getInstance().queryState;
        registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts("ShowSearchHistory"), searchTextField);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<String> history = getHistory(e.getProject());
        showCompletionPopup(history);
    }

    private void showCompletionPopup(List<String> list) {
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(list)
                .setMovable(false)
                .setResizable(false)
                .setRequestFocus(true)
                .setItemChosenCallback(item -> {
                    UIManager.setText(searchTextField, item);
                    IdeFocusManager.getGlobalInstance().requestFocus(searchTextField, false);
                })
                .createPopup()
                .showUnderneathOf(searchWrapper);
    }

    public List<String> getHistory(Project project) {
        String historyPropertyName = queryState.querySchema == JsonQuerySchema.JSONPath ? JSON_PATH_HISTORY_KEY : JMES_PATH_HISTORY_KEY;
        String history = PropertiesComponent.getInstance(project).getValue(historyPropertyName);
        return StrUtil.isNotBlank(history) ? StrUtil.split(history, '\n') : List.of();
    }

    private void setHistory(Collection<String> history) {
        String historyPropertyName = queryState.querySchema == JsonQuerySchema.JSONPath ? JSON_PATH_HISTORY_KEY : JMES_PATH_HISTORY_KEY;
        PropertiesComponent.getInstance().setValue(historyPropertyName, StrUtil.join("\n", history));
    }


    public void addHistory(Project project, String text) {
        if (StrUtil.isBlank(text)) {
            return;
        }

        ArrayDeque<String> history = new ArrayDeque<>(getHistory(project));
        if (!history.contains(text)) {
            history.addFirst(text);
            if (history.size() > 10) {
                history.removeLast();
            }
            setHistory(history);
        } else {
            if (history.getFirst().equals(text)) {
                return;
            }
            history.remove(text);
            history.addFirst(text);
            setHistory(history);
        }
    }
}