package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.service.persistent.converter.AttributeSerializationStateConverter;
import cn.memoryzy.json.service.persistent.converter.EditorAppearanceStateConverter;
import cn.memoryzy.json.service.persistent.converter.EditorBehaviorStateConverter;
import cn.memoryzy.json.service.persistent.state.AttributeSerializationState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.annotations.Attribute;
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

    /**
     * 属性序列化设置项
     */
    @Attribute(converter = AttributeSerializationStateConverter.class)
    public AttributeSerializationState attributeSerializationState;

    /**
     * 编辑器外观设置项
     */
    @Attribute(converter = EditorAppearanceStateConverter.class)
    public EditorAppearanceState editorAppearanceState;

    /**
     * 编辑器行为设置项
     */
    @Attribute(converter = EditorBehaviorStateConverter.class)
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
