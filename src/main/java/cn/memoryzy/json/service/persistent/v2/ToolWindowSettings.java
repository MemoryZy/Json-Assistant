package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.service.persistent.state.v2.ToolWindowState;
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
@State(name = "ToolWindow", storages = {@Storage(value = "Jsonx.xml")})
public class ToolWindowSettings implements PersistentStateComponent<ToolWindowState> {

    public static ToolWindowSettings getInstance(Project project) {
        return project.getService(ToolWindowSettings.class);
    }

    public ToolWindowState state = new ToolWindowState();

    @Override
    public @Nullable ToolWindowState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull ToolWindowState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
