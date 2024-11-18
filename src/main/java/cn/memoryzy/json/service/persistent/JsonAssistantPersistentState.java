package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.service.persistent.state.AttributeSerializationState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/11/18
 */
@State(name = "JsonAssistantPersistentState", storages = {@Storage(value = "JsonAssistantPersistentState.xml")})
public class JsonAssistantPersistentState implements PersistentStateComponent<JsonAssistantPersistentState> {

    public static JsonAssistantPersistentState getInstance() {
        return ApplicationManager.getApplication().getService(JsonAssistantPersistentState.class);
    }

    public AttributeSerializationState attributeSerializationState;
    public EditorAppearanceState editorAppearanceState;
    public EditorBehaviorState editorBehaviorState;

    @Override
    public @Nullable JsonAssistantPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JsonAssistantPersistentState state) {
        this.attributeSerializationState = state.attributeSerializationState;
        this.editorAppearanceState = state.editorAppearanceState;
        this.editorBehaviorState = state.editorBehaviorState;
    }
}
