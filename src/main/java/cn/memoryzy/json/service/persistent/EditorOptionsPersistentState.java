package cn.memoryzy.json.service.persistent;

import cn.memoryzy.json.enums.BackgroundColorMatchingEnum;
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
    public boolean importHistory = true;

    /**
     * 展示编辑器行号
     */
    public boolean displayLineNumbers = false;

    /**
     * 编辑器背景配色主题
     */
    public BackgroundColorMatchingEnum backgroundColorMatchingEnum = BackgroundColorMatchingEnum.DEFAULT;

    /**
     * 显示折叠轮廓
     */
    public boolean foldingOutline = PlatformUtil.isNewUi();

    // ---------------------------------------------------

    /**
     * 自动识别并转换其他格式数据（总开关）
     */
    public boolean recognizeOtherFormats = true;

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
        this.importHistory = state.importHistory;
        this.displayLineNumbers = state.displayLineNumbers;
        this.backgroundColorMatchingEnum = state.backgroundColorMatchingEnum;
        this.foldingOutline = state.foldingOutline;

        this.recognizeOtherFormats = state.recognizeOtherFormats;
        this.recognizeXmlFormat = state.recognizeXmlFormat;
        this.recognizeYamlFormat = state.recognizeYamlFormat;
        this.recognizeTomlFormat = state.recognizeTomlFormat;
        this.recognizeUrlParamFormat = state.recognizeUrlParamFormat;
    }
}
