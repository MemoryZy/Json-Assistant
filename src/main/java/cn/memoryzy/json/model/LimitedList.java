package cn.memoryzy.json.model;

import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList extends AbstractList<String> {

    private final List<String> history;
    private final int limit;

    public LimitedList(int limit) {
        this.history = new ArrayList<>(limit);
        this.limit = limit;
    }

    @Override
    public String get(int index) {
        return history.get(index);
    }

    @Override
    public int size() {
        return history.size();
    }

    @Override
    public boolean add(String element) {
        return history.add(element);
    }

    @Override
    public void add(int index, String element) {
        history.add(index, element);
    }

    public boolean add(String element, boolean isJson) {
        return addJson(element, isJson);
    }

    private boolean addJson(String element, boolean isJson) {
        // 即便是JSON5，也转为JSON再比较
        Object addObject = resolveJsonString(element, isJson);

        if (!isContains(addObject)) {
            add(0, element);
            if (history.size() > limit) {
                remove(history.size() - 1);
            }
        } else {
            String first = history.get(0);
            if (!deserializeThenCompare(first, addObject)) {
                deserializeCompareAndRemove(addObject);
                add(0, element);
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object element) {
        return history.contains(element);
    }

    @Override
    public String remove(int index) {
        return history.remove(index);
    }

    private boolean isContains(Object addObject) {
        for (String oriElement : history) {
            Object oriObject = resolveJsonString(oriElement, JsonUtil.isJson(oriElement));
            if (Objects.equals(addObject, oriObject)) {
                return true;
            }
        }

        return false;
    }

    private boolean deserializeThenCompare(String oriJsonStr, Object addObject) {
        return Objects.equals(resolveJsonString(oriJsonStr, JsonUtil.isJson(oriJsonStr)), addObject);
    }

    private void deserializeCompareAndRemove(Object addObject) {
        for (int i = history.size() - 1; i >= 0; i--) {
            String text = history.get(i);
            Object oriObject = resolveJsonString(text, JsonUtil.isJson(text));
            if (Objects.equals(addObject, oriObject)) {
                history.remove(i);
                break;
            }
        }
    }

    private static Object resolveJsonString(String element, boolean isJson) {
        // 即便是JSON5，也转为JSON再比较
        return isJson ? JsonUtil.parse(element) : JsonUtil.parse(Json5Util.convertJson5ToJson(element));
    }

}
