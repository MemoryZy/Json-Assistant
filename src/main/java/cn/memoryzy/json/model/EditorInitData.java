package cn.memoryzy.json.model;

import cn.memoryzy.json.enums.TextSourceType;

/**
 * 工具窗口编辑器初次打开时的默认信息
 *
 * @author Memory
 * @since 2024/11/26
 */
public class EditorInitData {

    /**
     * 是否存在有效文本
     */
    private final boolean hasText;

    /**
     * 有效Json文本
     */
    private final String jsonString;

    /**
     * 文本来源
     */
    private final TextSourceType sourceType;

    /**
     * 解析类型
     */
    private final String parseType;


    // region 构造器与Getter
    public EditorInitData(boolean hasText, String jsonString, TextSourceType sourceType, String parseType) {
        this.hasText = hasText;
        this.jsonString = jsonString;
        this.sourceType = sourceType;
        this.parseType = parseType;
    }

    public boolean isHasText() {
        return hasText;
    }

    public String getJsonString() {
        return jsonString;
    }

    public TextSourceType getSourceType() {
        return sourceType;
    }

    public String getParseType() {
        return parseType;
    }

    // endregion
}
