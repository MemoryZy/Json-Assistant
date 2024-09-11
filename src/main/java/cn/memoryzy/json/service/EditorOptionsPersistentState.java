package cn.memoryzy.json.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/9/11
 */
@State(name = "JsonAssistantEditorOptions", storages = {@Storage(value = "JsonAssistantEditorOptions.xml")})
public class EditorOptionsPersistentState implements PersistentStateComponent<EditorOptionsPersistentState> {

    public static EditorOptionsPersistentState getInstance() {
        return ApplicationManager.getApplication().getService(EditorOptionsPersistentState.class);
    }

    @Override
    public @Nullable EditorOptionsPersistentState getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull EditorOptionsPersistentState state) {

    }
}
