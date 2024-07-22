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
@State(name = "JsonAssistantViewerLastRecord")
public class JsonViewerLastRecordState implements PersistentStateComponent<JsonViewerLastRecordState> {

    public static JsonViewerLastRecordState getInstance(Project project) {
        return project.getService(JsonViewerLastRecordState.class);
    }

    public String record;

    @Override
    public @Nullable JsonViewerLastRecordState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonViewerLastRecordState state) {
        this.record = state.record;
    }
}
