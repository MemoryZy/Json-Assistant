package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.enums.JsonQueryLanguage;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * JSON 查询配置项
 *
 * @author Memory
 * @since 2024/12/27
 */
@Tag("query")
public class QueryState {

    /**
     * 当前使用的 JSON 查询语言类型
     */
    private JsonQueryLanguage queryLanguage = JsonQueryLanguage.JSONPath;

    /**
     * 显示原始文本编辑框
     */
    private boolean displayOriginalText = true;


    public void setQueryLanguage(JsonQueryLanguage queryLanguage) {
        this.queryLanguage = queryLanguage;
    }

    public void setDisplayOriginalText(boolean displayOriginalText) {
        this.displayOriginalText = displayOriginalText;
    }

    public JsonQueryLanguage getQueryLanguage() {
        return queryLanguage;
    }

    public boolean isDisplayOriginalText() {
        return displayOriginalText;
    }
}
