package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.EditorAppearanceStateV2;
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
@State(name = "Editor Appearance", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_FILE_NAME)})
public class EditorAppearanceSettings implements PersistentStateComponent<EditorAppearanceStateV2> {

    // public static EditorAppearanceSettings getInstance() {
    //     return ApplicationManager.getApplication().getService(EditorAppearanceSettings.class);
    // }

    public static EditorAppearanceSettings getInstance(Project project) {
        return project.getService(EditorAppearanceSettings.class);
    }

    /**
     * 编辑器外观设置项
     */
    public EditorAppearanceStateV2 state = new EditorAppearanceStateV2();

    @Override
    public @Nullable EditorAppearanceStateV2 getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull EditorAppearanceStateV2 state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
