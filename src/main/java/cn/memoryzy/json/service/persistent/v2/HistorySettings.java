package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.service.persistent.state.HistoryState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/17
 */
public class HistorySettings implements PersistentStateComponent<HistoryState> {

    public static HistorySettings getInstance() {
        return ApplicationManager.getApplication().getService(HistorySettings.class);
    }

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
