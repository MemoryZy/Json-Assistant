package cn.memoryzy.json.service;

import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/9/11
 */
@State(name = "JsonAssistantEditorOptions", storages = {@Storage(value = "JsonAssistantEditorOptions.xml")})
public class EditorOptionsPersistentState implements PersistentStateComponent<EditorOptionsPersistentState> {

    public static EditorOptionsPersistentState getInstance() {
        return ApplicationManager.getApplication().getService(EditorOptionsPersistentState.class);
    }

    /**
     * 自动导入最新 JSON 记录至编辑器
     */
    public boolean loadLastRecord = true;

    /**
     * 展示编辑器行号
     */
    public boolean displayLineNumbers = false;

    /**
     * 跟随主编辑器配色主题
     */
    public boolean followEditorTheme = true;

    /**
     * 显示折叠轮廓
     */
    public boolean foldingOutline = PlatformUtil.isNewUi();

    @Override
    public @Nullable EditorOptionsPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull EditorOptionsPersistentState state) {
        this.loadLastRecord = state.loadLastRecord;
        this.displayLineNumbers = state.displayLineNumbers;
        this.followEditorTheme = state.followEditorTheme;
        this.foldingOutline = state.foldingOutline;
    }
}
