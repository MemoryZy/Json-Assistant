package cn.memoryzy.json.model;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.LocalDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/10
 */
public class LimitedList extends AbstractList<String> {

    public static final String INSERT_TIME_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".eaf7c221ec5042c48355a9498a31d8ba.insertTime";

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

    /**
     * 添加Json元素（调用该方法默认为第一次添加此Json元素）
     *
     * @param element 元素
     * @param isJson  是否为Json，反之为Json5
     * @return 结果
     */
    public boolean add(String element, boolean isJson) {
        return addJson(element, isJson);
    }

    private boolean addJson(String element, boolean isJson) {
        // 即便是JSON5，也转为JSON再比较
        JsonWrapper addObject = resolveJsonString(element, isJson);

        if (!isContains(addObject)) {
            addFirstWithInsertTime(addObject);
            if (history.size() > limit) {
                remove(history.size() - 1);
            }
        } else {
            String first = history.get(0);
            if (!deserializeThenCompare(first, addObject)) {
                deserializeCompareAndRemove(addObject);
                addFirstWithInsertTime(addObject);
            }
        }
        return true;
    }

    /**
     * 将元素添加至第一位前给Json内添加时间戳
     *
     * @param addObject 添加的Json对象
     */
    private void addFirstWithInsertTime(JsonWrapper addObject) {
        if (addObject.isObject()) {
            // 添加时间戳（保持key唯一）
            ObjectWrapper objectWrapper = (ObjectWrapper) addObject;
            objectWrapper.put(INSERT_TIME_KEY, System.currentTimeMillis());
        } else if (addObject.isArray()) {
            // 往Array中添加一个对象，对象中存在一个时间戳
            ArrayWrapper arrayWrapper = (ArrayWrapper) addObject;
            ObjectWrapper objectWrapper = new ObjectWrapper();
            objectWrapper.put(INSERT_TIME_KEY, System.currentTimeMillis());
            arrayWrapper.add(objectWrapper);
        }

        add(0, addObject.toJsonString());
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

    private static JsonWrapper resolveJsonString(String element, boolean isJson) {
        // 即便是JSON5，也转为JSON再比较
        return isJson ? JsonUtil.parse(element) : Json5Util.parse(element);
    }


    /**
     * 解析添加时间并在Json中移除它
     *
     * @param jsonStr Json文本
     * @return left：Json文本，right：添加时间
     */
    public static ImmutablePair<String, LocalDateTime> readAndRemoveInsertTime(String jsonStr) {
        JsonWrapper jsonWrapper = resolveJsonString(jsonStr, JsonUtil.isJson(jsonStr));

        LocalDateTime localDateTime = null;
        if (jsonWrapper.isObject()) {
            // 添加时间戳（保持key唯一）
            ObjectWrapper objectWrapper = (ObjectWrapper) jsonWrapper;
            Object value = objectWrapper.get(INSERT_TIME_KEY);
            // 兼容原先没有添加时间的记录
            if (value instanceof Long) {
                localDateTime = LocalDateTimeUtil.of((Long) value);
                objectWrapper.remove(INSERT_TIME_KEY);
            }

        } else if (jsonWrapper.isArray()) {
            // 往Array中添加一个对象，对象中存在一个时间戳
            ArrayWrapper arrayWrapper = (ArrayWrapper) jsonWrapper;
            // 获取末尾的对象，并且移除该对象
            int lastIndex = arrayWrapper.size() - 1;
            Object obj = arrayWrapper.get(lastIndex);
            // 兼容原先没有添加时间的记录
            if (obj instanceof ObjectWrapper) {
                // 判断是否存在且仅有一个时间戳key
                ObjectWrapper objectWrapper = (ObjectWrapper) obj;
                if (objectWrapper.size() == 1 && objectWrapper.containsKey(INSERT_TIME_KEY)) {
                    Object insertTime = objectWrapper.get(INSERT_TIME_KEY);
                    if (insertTime instanceof Long) {
                        localDateTime = LocalDateTimeUtil.of((Long) insertTime);
                        // 移除该对象
                        arrayWrapper.remove(lastIndex);
                    }
                }
            }
        }

        return ImmutablePair.of(jsonWrapper.toJsonString(), localDateTime);
    }

}
