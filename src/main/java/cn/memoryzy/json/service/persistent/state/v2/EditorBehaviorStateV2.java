package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.enums.DataFormatType;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;

import java.util.List;

/**
 * @author Memory
 * @since 2025/6/17
 */
@Tag("properties")
public class EditorBehaviorStateV2 {

    /**
     * 是否自动识别并转换剪贴板中的非标准格式数据（如HTML、富文本等）（总开关）
     */
    private boolean autoRecognizeFormats = true;

    /**
     * 启用自动识别的数据格式列表（空列表表示禁用所有自动识别）
     */
    private List<String> enabledFormats = getDefaultFormatList();

    /**
     * 从剪贴板自动导入数据时是否显示确认对话框
     */
    private boolean shouldPromptBeforeImport = false;


    public void setAutoRecognizeFormats(boolean autoRecognizeFormats) {
        this.autoRecognizeFormats = autoRecognizeFormats;
    }

    public void setEnabledFormats(List<String> enabledFormats) {
        this.enabledFormats = enabledFormats;
    }

    public void setShouldPromptBeforeImport(boolean shouldPromptBeforeImport) {
        this.shouldPromptBeforeImport = shouldPromptBeforeImport;
    }

    public boolean isAutoRecognizeFormats() {
        return autoRecognizeFormats;
    }

    @XCollection(propertyElementName = "formats", elementName = "item", style = XCollection.Style.v2)
    public List<String> getEnabledFormats() {
        return enabledFormats;
    }

    public boolean isShouldPromptBeforeImport() {
        return shouldPromptBeforeImport;
    }

    private List<String> getDefaultFormatList() {
        return List.of(DataFormatType.XML.name(), DataFormatType.YAML.name(), DataFormatType.TOML.name(), DataFormatType.URL_PARAM.name());
    }

}
