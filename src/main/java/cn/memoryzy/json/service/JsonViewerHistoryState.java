package cn.memoryzy.json.service;

import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.converter.LimitedListConverter;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/22
 */
@State(name = "JsonAssistantJsonViewerHistory")
public class JsonViewerHistoryState implements PersistentStateComponent<JsonViewerHistoryState> {

    public static final int HISTORY_LIMIT = 25;

    public static JsonViewerHistoryState getInstance(Project project) {
        return project.getService(JsonViewerHistoryState.class);
    }

    @Attribute(converter = LimitedListConverter.class)
    public LimitedList<String> historyList;

    @Override
    public @Nullable JsonViewerHistoryState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewerHistoryState state) {
        this.historyList = state.historyList;
    }

    public LimitedList<String> getHistory() {
        initHistory();
        return historyList;
    }

    private void initHistory() {
        if (Objects.isNull(historyList)) {
            historyList = new LimitedList<>(HISTORY_LIMIT);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonViewerHistoryState that = (JsonViewerHistoryState) o;
        return Objects.equals(historyList, that.historyList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyList);
    }
}
