package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import cn.memoryzy.json.service.persistent.state.v2.SerializationState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/17
 */
@State(name = "Serialization/Deserialization", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public class SerializationSettings implements PersistentStateComponent<SerializationSettings> {

     public static SerializationSettings getInstance() {
         return ApplicationManager.getApplication().getService(SerializationSettings.class);
     }

    /**
     * 序列化相关配置项
     */
    private SerializationState serializationState = new SerializationState();

    /**
     * 反序列化相关配置项
     */
    private DeserializationState deserializationState = new DeserializationState();


    @Override
    public SerializationSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SerializationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    public void setSerializationState(SerializationState serializationState) {
        this.serializationState = serializationState;
    }

    public void setDeserializationState(DeserializationState deserializationState) {
        this.deserializationState = deserializationState;
    }

    @Property(surroundWithTag = false)
    public SerializationState getSerializationState() {
        return serializationState;
    }

    @Property(surroundWithTag = false)
    public DeserializationState getDeserializationState() {
        return deserializationState;
    }
}
