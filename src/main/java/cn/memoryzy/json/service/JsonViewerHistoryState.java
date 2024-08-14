package cn.memoryzy.json.service;

import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.converter.LimitedListConverter;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @OptionTag(converter = LimitedListConverter.class)
    public LimitedList<String> historyList;

    @Override
    public @Nullable JsonViewerHistoryState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewerHistoryState state) {
        this.historyList = state.historyList;
    }

    public List<String> getHistoryList() {
        initHistoryList();
        return historyList;
    }

    private void initHistoryList() {
        if (Objects.isNull(historyList)) {
            historyList = new LimitedList<>(HISTORY_LIMIT);
        }
    }
}
