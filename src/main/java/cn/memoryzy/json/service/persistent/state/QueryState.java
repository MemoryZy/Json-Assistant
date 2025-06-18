package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.JsonQueryLanguage;

/**
 * @author Memory
 * @since 2024/12/27
 */
@Deprecated
public class QueryState {

    /**
     * 当前查询语言
     */
    public JsonQueryLanguage querySchema = JsonQueryLanguage.JSONPath;

    /**
     * 显示原始文本
     */
    public boolean showOriginalText = true;

}
