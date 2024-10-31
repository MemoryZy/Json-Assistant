package cn.memoryzy.json.service.persistent;

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

    /**
     * 自动识别并转换 XML 格式为 JSON 数据
     */
    public boolean recognizeXmlFormat = true;

    /**
     * 自动识别并转换 YAML 格式为 JSON 数据
     */
    public boolean recognizeYamlFormat = true;

    /**
     * 自动识别并转换 TOML 格式为 JSON 数据
     */
    public boolean recognizeTomlFormat = true;

    /**
     * 自动识别并转换 URL Param 格式为 JSON 数据
     */
    public boolean recognizeUrlParamFormat = true;


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

        this.recognizeXmlFormat = state.recognizeXmlFormat;
        this.recognizeYamlFormat = state.recognizeYamlFormat;
        this.recognizeTomlFormat = state.recognizeTomlFormat;
        this.recognizeUrlParamFormat = state.recognizeUrlParamFormat;
    }
}
