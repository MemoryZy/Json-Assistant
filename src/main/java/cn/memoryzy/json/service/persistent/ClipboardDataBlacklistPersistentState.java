package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.model.JsonEntry;
import cn.memoryzy.json.service.persistent.converter.BlacklistConverter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * @author Memory
 * @since 2025/3/3
 */
@State(name = "Json Assistant Clipboard Data Blacklist", storages = {@Storage(value = "ClipboardDataBlacklistPersistentState.xml")})
public class ClipboardDataBlacklistPersistentState implements PersistentStateComponent<ClipboardDataBlacklistPersistentState>  {

    public static ClipboardDataBlacklistPersistentState getInstance() {
        return ApplicationManager.getApplication().getService(ClipboardDataBlacklistPersistentState.class);
    }

    @Attribute(converter = BlacklistConverter.class)
    public LinkedList<JsonEntry> blacklist = new LinkedList<>();

    @Override
    public @Nullable ClipboardDataBlacklistPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ClipboardDataBlacklistPersistentState state) {
        this.blacklist = state.blacklist;
    }
}
