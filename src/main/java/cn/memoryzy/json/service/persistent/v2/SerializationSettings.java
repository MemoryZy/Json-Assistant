package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import cn.memoryzy.json.service.persistent.state.v2.SerializationStateV2;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/17
 */
@State(name = "Serialization/Deserialization", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_FILE_NAME)})
public class SerializationSettings implements PersistentStateComponent<SerializationSettings> {

    // public static SerializationSettings getInstance() {
    //     return ApplicationManager.getApplication().getService(SerializationSettings.class);
    // }

    public static SerializationSettings getInstance(Project project) {
        return project.getService(SerializationSettings.class);
    }

    private SerializationStateV2 serializationState;

    private DeserializationState deserializationState;


    @Override
    public SerializationSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SerializationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setSerializationState(SerializationStateV2 serializationState) {
        this.serializationState = serializationState;
    }

    public void setDeserializationState(DeserializationState deserializationState) {
        this.deserializationState = deserializationState;
    }

    public SerializationStateV2 getSerializationState() {
        return serializationState;
    }

    public DeserializationState getDeserializationState() {
        return deserializationState;
    }
}
