package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.JsonQuerySchema;

/**
 * @author Memory
 * @since 2024/12/27
 */
public class QueryState {

    /**
     * 当前查询语言
     */
    public JsonQuerySchema querySchema = JsonQuerySchema.JSONPath;

    /**
     * 显示原始文本
     */
    public boolean showOriginalText = true;

}
