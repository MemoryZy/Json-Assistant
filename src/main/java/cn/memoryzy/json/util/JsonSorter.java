package cn.memoryzy.json.util;

import cn.memoryzy.json.model.sort.KeyValuePair;
import cn.memoryzy.json.model.sort.SortStrategy;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class JsonSorter {

    public static JsonWrapper sortJson(String json, SortStrategy strategy) {
        JsonWrapper wrapper = JsonUtil.isJson(json) ? JsonUtil.parse(json) : Json5Util.parse(json);
        return null == wrapper ? null : (JsonWrapper) sort(wrapper, strategy);
    }

    public static JsonWrapper sortJson(String json, boolean isJson, SortStrategy strategy) {
        JsonWrapper wrapper = isJson ? JsonUtil.parse(json) : Json5Util.parse(json);
        return null == wrapper ? null : (JsonWrapper) sort(wrapper, strategy);
    }

    /**
     * 对数据进行排序
     *
     * @param data     数据
     * @param strategy 策略
     * @return 排序结果
     */
    private static Object sort(Object data, SortStrategy strategy) {
        if (data instanceof ObjectWrapper) {
            return sortObject((ObjectWrapper) data, strategy);
        } else if (data instanceof ArrayWrapper) {
            return sortArray((ArrayWrapper) data, strategy);
        }
        return data;
    }

    /**
     * 对 对象类型 进行排序
     *
     * @param wrapper  Map对象
     * @param strategy 策略
     * @return 排序结果
     */
    private static ObjectWrapper sortObject(ObjectWrapper wrapper, SortStrategy strategy) {
        List<KeyValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : wrapper.entrySet()) {
            pairs.add(new KeyValuePair(entry.getKey(), sort(entry.getValue(), strategy)));
        }

        List<KeyValuePair> sortedPairs = strategy.sortPairs(pairs);
        ObjectWrapper sortedMap = new ObjectWrapper();
        sortedPairs.forEach(pair -> sortedMap.put(pair.getKey(), pair.getValue()));

        return sortedMap;
    }

    /**
     * 对 数组类型 进行排序（不处理数组本身，只处理其中嵌套的对象类型。因为通常数组排序都是有意义的）
     *
     * @param wrapper  数组对象
     * @param strategy 策略
     * @return 排序结果
     */
    private static ArrayWrapper sortArray(ArrayWrapper wrapper, SortStrategy strategy) {
        ArrayWrapper sortedList = new ArrayWrapper();
        for (Object element : wrapper) {
            sortedList.add(sort(element, strategy));
        }
        return sortedList;
    }

}
