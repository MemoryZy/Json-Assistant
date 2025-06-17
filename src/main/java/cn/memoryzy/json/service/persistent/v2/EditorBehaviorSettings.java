package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorStateV2;
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
@State(name = "Editor Behavior", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_FILE_NAME)})
public class EditorBehaviorSettings implements PersistentStateComponent<EditorBehaviorStateV2> {

    // public static EditorBehaviorSettings getInstance() {
    //     return ApplicationManager.getApplication().getService(EditorBehaviorSettings.class);
    // }

    public static EditorBehaviorSettings getInstance(Project project) {
        return project.getService(EditorBehaviorSettings.class);
    }

    public EditorBehaviorStateV2 state = new EditorBehaviorStateV2();

    @Override
    public @Nullable EditorBehaviorStateV2 getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull EditorBehaviorStateV2 state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
