package cn.memoryzy.json.service.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/10/24
 */
@State(name = "JsonAssistantAttributeSerializationPersistentState", storages = {@Storage(value = "JsonAssistantAttributeSerializationPersistentState.xml")})
public class AttributeSerializationPersistentState implements PersistentStateComponent<AttributeSerializationPersistentState> {

    public static AttributeSerializationPersistentState getInstance() {
        return ApplicationManager.getApplication().getService(AttributeSerializationPersistentState.class);
    }

    /**
     * 是否包含随机值（属性序列化为 JSON 时）
     */
    public boolean includeRandomValues = false;

    /**
     * 识别 FastJson 注解
     */
    public boolean recognitionFastJsonAnnotation = true;

    /**
     * 识别 Jackson 注解
     */
    public boolean recognitionJacksonAnnotation = true;

    @Override
    public @Nullable AttributeSerializationPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AttributeSerializationPersistentState state) {
        this.includeRandomValues = state.includeRandomValues;
        this.recognitionFastJsonAnnotation = state.recognitionFastJsonAnnotation;
        this.recognitionJacksonAnnotation = state.recognitionJacksonAnnotation;
    }
}
