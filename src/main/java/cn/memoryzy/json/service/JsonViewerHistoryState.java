package cn.memoryzy.json.service;

import cn.memoryzy.json.model.LimitedList;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/22
 */
@State(name = "JsonAssistantViewerHistory")
public class JsonViewerHistoryState implements PersistentStateComponent<JsonViewerHistoryState> {

    public static final int HISTORY_LIMIT = 20;

    public static JsonViewerHistoryState getInstance(Project project) {
        return project.getService(JsonViewerHistoryState.class);
    }

    public List<String> historyList;

    @Override
    public @Nullable JsonViewerHistoryState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewerHistoryState state) {
        this.historyList = state.historyList;
    }

    public void initHistoryList() {
        if (Objects.isNull(historyList)) {
            historyList = new LimitedList<>(HISTORY_LIMIT);
        }
    }
}
