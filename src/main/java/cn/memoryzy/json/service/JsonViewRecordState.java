package cn.memoryzy.json.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/7/22
 */
@State(name = "JsonAssistantViewRecord")
public class JsonViewRecordState implements PersistentStateComponent<JsonViewRecordState> {

    public static JsonViewRecordState getInstance(Project project) {
        return project.getService(JsonViewRecordState.class);
    }

    public String record;

    @Override
    public @Nullable JsonViewRecordState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewRecordState state) {
        this.record = state.record;
    }
}
