package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
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
@State(name = "History", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public class HistorySettings implements PersistentStateComponent<HistoryState> {

    // public static HistorySettings getInstance() {
    //     return ApplicationManager.getApplication().getService(HistorySettings.class);
    // }

    public static HistorySettings getInstance(Project project) {
        return project.getService(HistorySettings.class);
    }

    /**
     * 历史记录设置项
     */
    public HistoryState state = new HistoryState();


    @Override
    public @Nullable HistoryState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull HistoryState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
