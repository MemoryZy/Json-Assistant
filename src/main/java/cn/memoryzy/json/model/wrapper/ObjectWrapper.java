package cn.memoryzy.json.model.wrapper;

import cn.memoryzy.json.util.Json5Util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义Map，保持存入顺序
 *
 * @author Memory
 * @since 2024/11/11
 */
public class ObjectWrapper extends LinkedHashMap<String, Object> implements JsonWrapper {

    public ObjectWrapper() {
        super();
    }

    @SuppressWarnings("unchecked")
    public ObjectWrapper(Object source) {
        super();
        if (!(source instanceof Map)) {
            throw new IllegalArgumentException("source is not a Map: " + source);
        }

        initMap((Map<String, Object>) source, true);
    }

    @SuppressWarnings("unchecked")
    public ObjectWrapper(Object source, boolean containsCommentKey) {
        super();
        if (!(source instanceof Map)) {
            throw new IllegalArgumentException("source is not a Map: " + source);
        }

        initMap((Map<String, Object>) source, containsCommentKey);
    }


    private void initMap(Map<String, Object> source, boolean containsCommentKey) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                boolean isCommentKey = Json5Util.isCommentKey(key, value);
                if (containsCommentKey) {
                    // 特殊处理 注释 键
                    if (isCommentKey) {
                        put(key, value);
                    } else {
                        // 递归转换嵌套的 Map
                        put(key, new ObjectWrapper(value));
                    }

                } else {
                    if (!isCommentKey) {
                        put(key, new ObjectWrapper(value));
                    }
                }

            } else if (value instanceof Collection) {
                // 转换嵌套的 List
                put(key, new ArrayWrapper(value));
            } else {
                // 直接添加其他类型的值
                put(key, value);
            }
        }
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean noItems() {
        return isEmpty();
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public ObjectWrapper cloneAndRemoveCommentKey() {
        return new ObjectWrapper(this, false);
    }

    public static boolean isWrapper(Object object) {
        return object instanceof ObjectWrapper;
    }

}
