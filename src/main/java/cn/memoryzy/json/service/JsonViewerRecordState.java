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
@State(name = "JsonAssistantViewerRecord")
public class JsonViewerRecordState implements PersistentStateComponent<JsonViewerRecordState> {

    public static JsonViewerRecordState getInstance(Project project) {
        return project.getService(JsonViewerRecordState.class);
    }

    public String record;

    @Override
    public @Nullable JsonViewerRecordState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewerRecordState state) {
        this.record = state.record;
    }
}
