package cn.memoryzy.json.util;

import a2u.tn.utils.json.MapNavigator;
import a2u.tn.utils.json.TnJson;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

/**
 * Json5 处理
 *
 * @author Memory
 * @since 2024/11/6
 */
public class Json5Util {

    /**
     * 用这个值代替 Map 中的 Double类型变量（如果为 infinite 或 NaN）
     */
    public static final double ALMOST_INFINITE = Double.MAX_VALUE - 1.0;
    public static final double ALMOST_NAN = Double.MAX_VALUE - 2.0;

    public static boolean isJson5(String text) {
        Object data = tryResolveJson5(text);
        return data instanceof Map || data instanceof List;
    }

    public static boolean isJson5Array(String text) {
        return tryResolveJson5(text) instanceof List;
    }

    public static boolean isJson5Object(String text) {
        return tryResolveJson5(text) instanceof Map;
    }


    public static String formatJson5(String json) {
        return toJson5(tryResolveJson5(json), TnJson.Mode.JSON5);
    }

    /**
     * 将Json压缩成一行
     *
     * @param json json字符串
     * @return 压缩Json
     */
    public static String compressJson5(String json) {
        return toJson5(tryResolveJson5(json), TnJson.Mode.JSON5COMPACT);
    }

    public static String toJson5(Object obj) {
        return toJson5(obj, TnJson.Mode.JSON5);
    }

    public static String jsonToJson5(String jsonStr) {
        return toJson5(JSONUtil.parse(jsonStr));
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        Object object = tryResolveJson5(json);
        return (object instanceof Map) ? (Map<String, Object>) object : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> toList(String json) {
        Object object = tryResolveJson5(json);
        return (object instanceof List) ? (List<Object>) object : null;
    }

    public static String toJson5(Object obj, TnJson.Mode mode) {
        try {
            return TnJson.toJson(obj, mode);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析Json5文本，若解析失败，返回null
     *
     * @param text 文本
     * @return 若为对象，则返回 Map；若为 List，则返回 List；否则返回 null
     */
    public static Object tryResolveJson5(String text) {
        // 判断是否为 Json，再判断是否为 Json5
        return JsonUtil.isJson(text) ? null : resolveJson5(text);
    }

    public static Object resolveJson5(String text) {
        try {
            Map<String, Object> map = TnJson.parse(text);
            if (MapUtil.isNotEmpty(map)) {
                // 若是 Array，那么 Map 中只会存在一个 key，且 key 为 list
                if (map.size() == 1) {
                    Object list = MapNavigator.fromPath(map, TnJson.DEFAULT_LIST_KEY);
                    if (list instanceof List) return list;
                }

                // 不为 List，表示为 Json5 Object
                return map;
            }
        } catch (Exception ignored) {
        }

        return null;
    }


    @SuppressWarnings("unchecked")
    public static void cleanMapList(List<Object> mapList) {
        for (Object object : mapList) {
            if (object instanceof Map) {
                cleanMap((Map<String, Object>) object);
            }
        }
    }


    /**
     * 递归地清理Map中的Double值，如果遇到Infinity或NaN则替换为默认值。
     * <p>HuTool的{@link JSONObject}类不允许Infinity或NaN值的存在</p>
     *
     * @param map 需要清理的Map
     */
    @SuppressWarnings("unchecked")
    public static void cleanMap(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Double) {
                double doubleValue = (Double) value;
                if (Double.isInfinite(doubleValue)) {
                    // 如果是Infinity或NaN，则替换为默认值
                    map.put(key, ALMOST_INFINITE);
                }

                if (Double.isNaN(doubleValue)) {
                    map.put(key, ALMOST_NAN);
                }
            } else if (value instanceof Map) {
                // 如果值是另一个Map，则递归调用此方法
                cleanMap((Map<String, Object>) value);
            }
        }
    }

}
