package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.service.persistent.state.GeneralState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/6/17
 */
public class GeneralSettings implements PersistentStateComponent<GeneralState> {

    public static GeneralSettings getInstance() {
        return ApplicationManager.getApplication().getService(GeneralSettings.class);
    }

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
