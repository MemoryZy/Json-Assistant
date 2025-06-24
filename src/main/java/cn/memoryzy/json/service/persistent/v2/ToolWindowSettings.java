package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.EditorBehaviorState;
import cn.memoryzy.json.service.persistent.state.v2.EditorVisualState;
import cn.memoryzy.json.service.persistent.state.v2.HistoryState;
import cn.memoryzy.json.service.persistent.state.v2.QueryState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/17
 */
@Service(Service.Level.APP)
@State(name = "ToolWindow", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_MAIN_FILE)})
public final class ToolWindowSettings implements PersistentStateComponent<ToolWindowSettings> {

    public static ToolWindowSettings getInstance() {
        return ApplicationManager.getApplication().getService(ToolWindowSettings.class);
    }

    /**
     * 配置版本
     */
    private Integer version = JsonAssistantPlugin.CONFIG_VERSION;

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

    /**
     * 历史记录设置项
     */
    public HistoryState historyState = new HistoryState();


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

    public void setHistoryState(HistoryState historyState) {
        this.historyState = historyState;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    @Property(surroundWithTag = false)
    public HistoryState getHistoryState() {
        return historyState;
    }

    @Attribute
    public Integer getVersion() {
        return version;
    }
}
