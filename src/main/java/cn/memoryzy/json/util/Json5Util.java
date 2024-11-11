package cn.memoryzy.json.util;

import a2u.tn.utils.json.MapNavigator;
import a2u.tn.utils.json.TnJson;
import a2u.tn.utils.json.TnJsonBuilder;
import cn.hutool.core.map.MapUtil;
import cn.memoryzy.json.model.deserialize.ArrayWrapper;
import cn.memoryzy.json.model.deserialize.JsonWrapper;
import cn.memoryzy.json.model.deserialize.ObjectWrapper;

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
        return toJson5Str(tryResolveJson5(json), FORMAT_JSON5);
    }

    /**
     * 将Json压缩成一行
     *
     * @param json json字符串
     * @return 压缩Json
     */
    public static String compressJson5(String json) {
        return toJson5Str(tryResolveJson5(json), COMPACT_JSON5);
    }


    public static JsonWrapper parse(String json) {
        if (isJson5Object(json)) {
            return parseObject(json);
        } else if (isJson5Array(json)) {
            return parseArray(json);
        }
        return null;
    }

    public static ObjectWrapper parseObject(String text) {
        return new ObjectWrapper(resolveJson5(text));
    }

    public static ArrayWrapper parseArray(String text) {
        return new ArrayWrapper(resolveJson5(text));
    }


    public static String convertJson5ToJson(String json5Str) {
        Object data = resolveJson5(json5Str);
        return Objects.isNull(data) ? null : JsonUtil.formatJson(data);
    }

    public static String convertJsonToJson5(String jsonStr) {
        return toJson5Str(JsonUtil.parse(jsonStr));
    }


    public static String toJson5Str(Object obj) {
        return toJson5Str(obj, FORMAT_JSON5);
    }


    public static String toJson5Str(Object obj, TnJsonBuilder builder) {
        try {
            return builder.buildJson(obj);
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

}
