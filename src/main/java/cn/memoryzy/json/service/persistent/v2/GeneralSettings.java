package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.GeneralState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/17
 */
@State(name = "General", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public class GeneralSettings implements PersistentStateComponent<GeneralState> {

    // public static GeneralSettings getInstance() {
    //     return ApplicationManager.getApplication().getService(GeneralSettings.class);
    // }

    public static GeneralSettings getInstance(Project project) {
        return project.getService(GeneralSettings.class);
    }

    /**
     * 常规设置项
     */
    public GeneralState state = new GeneralState();


    @Override
    public @Nullable GeneralState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull GeneralState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
