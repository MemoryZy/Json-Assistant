package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.DataStorages;
import cn.memoryzy.json.service.persistent.state.GeneralState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
@State(name = "General", storages = {@Storage(value = DataStorages.STORAGE_MAIN_FILE)})
public final class GeneralSettings implements PersistentStateComponent<GeneralState> {

    public static GeneralSettings getInstance() {
        return ApplicationManager.getApplication().getService(GeneralSettings.class);
    }

    /**
     * 常规设置项
     */
    public GeneralState state = new GeneralState();


    @Override
    public @NotNull GeneralState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull GeneralState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
