package cn.memoryzy.json.service.persistent.state;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.enums.DataFormatType;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;

import java.util.Set;

/**
 * 编辑器行为设置项
 *
 * @author Memory
 * @since 2025/6/17
 */
@Tag("editor-behavior")
public class EditorBehaviorState {

    /**
     * 是否自动识别并转换剪贴板中的非标准格式数据（如XML、YAML等）（总开关）
     */
    private boolean autoRecognizeFormats = true;

    /**
     * 启用自动识别的数据格式列表（空列表表示禁用所有自动识别）
     */
    private Set<DataFormatType> enabledFormats = CollUtil.newHashSet(DataFormatType.XML, DataFormatType.YAML, DataFormatType.TOML, DataFormatType.URL_PARAM);

    /**
     * 是否将修改作用于源文件（外部 JSON 文件）
     */
    private boolean shouldApplyToSource = false;

    public void setAutoRecognizeFormats(boolean autoRecognizeFormats) {
        this.autoRecognizeFormats = autoRecognizeFormats;
    }

    public void setEnabledFormats(Set<DataFormatType> enabledFormats) {
        this.enabledFormats = enabledFormats;
    }

    public void setShouldApplyToSource(boolean shouldApplyToSource) {
        this.shouldApplyToSource = shouldApplyToSource;
    }


    public boolean isAutoRecognizeFormats() {
        return autoRecognizeFormats;
    }

    @XCollection(propertyElementName = "formats", elementName = "item", style = XCollection.Style.v2)
    public Set<DataFormatType> getEnabledFormats() {
        return enabledFormats;
    }

    public boolean isShouldApplyToSource() {
        return shouldApplyToSource;
    }

}
