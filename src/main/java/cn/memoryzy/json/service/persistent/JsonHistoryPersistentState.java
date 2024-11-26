package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.service.persistent.converter.HistoryLimitedListConverter;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/25
 */
@State(name = "JsonAssistantHistoryState", storages = {@Storage(value = "JsonAssistantHistoryState.xml")})
public class JsonHistoryPersistentState implements PersistentStateComponent<JsonHistoryPersistentState> {
    public static final int LIMIT = 25;

    public static JsonHistoryPersistentState getInstance(Project project) {
        return project.getService(JsonHistoryPersistentState.class);
    }

    @Attribute(converter = HistoryLimitedListConverter.class)
    public HistoryLimitedList history;

    @Override
    public @Nullable JsonHistoryPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonHistoryPersistentState state) {
        this.history = state.history;
    }

    public HistoryLimitedList getHistory() {
        if (Objects.isNull(this.history)) {
            this.history = new HistoryLimitedList(LIMIT);
        }

        return this.history;
    }
}
