package cn.memoryzy.json.model.deserialize;

import cn.memoryzy.json.util.JsonUtil;

/**
 * @author Memory
 * @since 2024/11/11
 */
public interface JsonWrapper {

    boolean isObject();

    boolean isArray();

    default String toJsonString() {
        return JsonUtil.formatJson(this);
    }

}
