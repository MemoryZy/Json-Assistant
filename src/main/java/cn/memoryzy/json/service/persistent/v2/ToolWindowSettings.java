package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.EditorVisualState;
import cn.memoryzy.json.service.persistent.state.v2.QueryState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/17
 */
@State(name = "ToolWindow", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public class ToolWindowSettings implements PersistentStateComponent<ToolWindowSettings> {

    public static ToolWindowSettings getInstance(Project project) {
        return project.getService(ToolWindowSettings.class);
    }

    /**
     * 窗口编辑器外观状态类
     */
    private EditorVisualState visualState = new EditorVisualState();

    /**
     * 窗口编辑器行为状态类
     */
    private EditorBehaviorState behaviorState = new EditorBehaviorState();

    /**
     * JSON 查询配置项
     */
    private QueryState queryState = new QueryState();


    @Override
    public @Nullable ToolWindowSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ToolWindowSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    public void setVisualState(EditorVisualState visualState) {
        this.visualState = visualState;
    }

    public void setBehaviorState(EditorBehaviorState behaviorState) {
        this.behaviorState = behaviorState;
    }

    public void setQueryState(QueryState queryState) {
        this.queryState = queryState;
    }

    @Property(surroundWithTag = false)
    public EditorVisualState getVisualState() {
        return visualState;
    }

    @Property(surroundWithTag = false)
    public EditorBehaviorState getBehaviorState() {
        return behaviorState;
    }

    @Property(surroundWithTag = false)
    public QueryState getQueryState() {
        return queryState;
    }
}
