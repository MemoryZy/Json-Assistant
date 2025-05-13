package cn.memoryzy.json.model.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Memory
 * @since 2024/11/11
 */
public class ArrayWrapper extends ArrayList<Object> implements JsonWrapper {

    @SuppressWarnings("unchecked")
    public ArrayWrapper(Object source) {
        super();
        if (!(source instanceof Collection)) {
            throw new IllegalArgumentException("source is not a Collection: " + source);
        }

        initCollection((Collection<Object>) source, true);
    }

    @SuppressWarnings("unchecked")
    public ArrayWrapper(Object source, boolean containsCommentKey) {
        super();
        if (!(source instanceof Collection)) {
            throw new IllegalArgumentException("source is not a Collection: " + source);
        }

        initCollection((Collection<Object>) source, containsCommentKey);
    }

    private void initCollection(Collection<Object> source, boolean containsCommentKey) {
        for (Object item : source) {
            if (item instanceof Map) {
                // 递归转换嵌套的 Map
                add(new ObjectWrapper(item, containsCommentKey));
            } else if (item instanceof Collection) {
                // 转换嵌套的 List
                add(new ArrayWrapper(item, containsCommentKey));
            } else {
                // 直接添加其他类型的值
                add(item);
            }
        }
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
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
    public ArrayWrapper cloneAndRemoveCommentKey() {
        return new ArrayWrapper(this, false);
    }

    public static boolean isWrapper(Object object) {
        return object instanceof ArrayWrapper;
    }

}
