package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.persistent.converter.LimitedListConverter;
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
@State(name = "JsonAssistantJsonHistory")
public class JsonHistoryPersistentState implements PersistentStateComponent<JsonHistoryPersistentState> {

    public static final int HISTORY_LIMIT = 25;

    public static JsonHistoryPersistentState getInstance(Project project) {
        return project.getService(JsonHistoryPersistentState.class);
    }

    @Attribute(converter = LimitedListConverter.class)
    public LimitedList historyList;

    @Override
    public @Nullable JsonHistoryPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonHistoryPersistentState state) {
        this.historyList = state.historyList;
    }

    public LimitedList getHistory() {
        if (Objects.isNull(historyList)) {
            historyList = new LimitedList(HISTORY_LIMIT);
        }
        return historyList;
    }
}
