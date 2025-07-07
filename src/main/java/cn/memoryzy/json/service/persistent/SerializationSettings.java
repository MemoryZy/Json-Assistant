package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.DeserializationState;
import cn.memoryzy.json.service.persistent.state.SerializationState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
@State(name = "Serialization/Deserialization", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public final class SerializationSettings implements PersistentStateComponent<SerializationSettings> {

    public static SerializationSettings getInstance() {
        return ApplicationManager.getApplication().getService(SerializationSettings.class);
    }

    /**
     * 配置版本
     */
    private Integer version = JsonAssistantPlugin.CONFIG_VERSION;

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

    @Attribute
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
