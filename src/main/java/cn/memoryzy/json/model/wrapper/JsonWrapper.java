package cn.memoryzy.json.model.wrapper;

import cn.memoryzy.json.util.JsonUtil;

/**
 * @author Memory
 * @since 2024/11/11
 */
public interface JsonWrapper {

    boolean isObject();

    boolean isArray();

    boolean noItems();

    JsonWrapper cloneAndRemoveCommentKey();

    default String toJsonString() {
        return JsonUtil.formatJson(this);
    }

}
