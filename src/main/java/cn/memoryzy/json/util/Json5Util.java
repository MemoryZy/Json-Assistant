package cn.memoryzy.json.util;

import a2u.tn.utils.json.MapNavigator;
import a2u.tn.utils.json.TnJson;
import a2u.tn.utils.json.TnJsonBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Json5 处理
 *
 * @author Memory
 * @since 2024/11/6
 */
public class Json5Util {

    /**
     * 指代 Map 中的 Double类型 POSITIVE_INFINITY 变量
     */
    public static final double POSITIVE_INFINITY = Double.MAX_VALUE - 1.0;

    /**
     * 指代 Map 中的 Double类型 NEGATIVE_INFINITY 变量
     */
    public static final double NEGATIVE_INFINITY = -(Double.MAX_VALUE - 1.0);

    /**
     * 指代 Map 中的 Double类型 NaN 变量
     */
    public static final double NAN = Double.MIN_VALUE - 1.0;

    /**
     * Json5格式化、单引号包裹字符串、保持null元素（若允许多行文本，在编辑器会有展示问题）
     */
    private static final TnJsonBuilder FORMAT_JSON5 = TnJson.builder().readable().formated().withoutKeyQuote().singleQuote().keepNull()/*.allowMultiRowString()*/;

    /**
     * Json5压缩、保持null元素
     */
    private static final TnJsonBuilder COMPACT_JSON5 = TnJson.builder().readable().withoutKeyQuote().singleQuote().keepNull();

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
        return toJson5(tryResolveJson5(json), FORMAT_JSON5);
    }

    /**
     * 将Json压缩成一行
     *
     * @param json json字符串
     * @return 压缩Json
     */
    public static String compressJson5(String json) {
        return toJson5(tryResolveJson5(json), COMPACT_JSON5);
    }

    public static String json5ToJson(String json5Str) {
        JSON json = toHuToolJson(json5Str);
        return Objects.isNull(json) ? null : json.toJSONString(2);
    }

    public static String jsonToJson5(String jsonStr) {
        return toJson5(JSONUtil.parse(jsonStr));
    }

    public static String toJson5(Object obj) {
        return toJson5(obj, FORMAT_JSON5);
    }

    public static String toJson5(Object obj, TnJsonBuilder builder) {
        try {
            return builder.buildJson(obj);
        } catch (Exception e) {
            return null;
        }
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

    @SuppressWarnings("unchecked")
    public static JSON toHuToolJson(String json5Str) {
        Object object = resolveJson5(json5Str);
        if (object instanceof List) {
            cleanMapList((List<Object>) object);
            return new JSONArray(object);
        } else if (object instanceof Map) {
            cleanMap((Map<String, Object>) object);
            return new JSONObject(object);
        }

        return null;
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
                if (doubleValue == Double.POSITIVE_INFINITY) {
                    map.put(key, POSITIVE_INFINITY);
                } else if (doubleValue == Double.NEGATIVE_INFINITY){
                    map.put(key, NEGATIVE_INFINITY);
                } else if (Double.isNaN(doubleValue)) {
                    map.put(key, NAN);
                }
            } else if (value instanceof Map) {
                // 如果值是另一个Map，则递归调用此方法
                cleanMap((Map<String, Object>) value);
            }
        }
    }

}
