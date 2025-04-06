package cn.memoryzy.json.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import org.apache.commons.lang3.StringEscapeUtils;
import thirdparty.a2u.tn.utils.json.MapNavigator;
import thirdparty.a2u.tn.utils.json.TnJson;
import thirdparty.a2u.tn.utils.json.TnJsonBuilder;

import java.util.*;

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
    public static final TnJsonBuilder FORMAT_JSON5 = TnJson.builder().readable().formated().withoutKeyQuote().singleQuote().keepNull().allowComments()/*.allowMultiRowString()*/;

    /**
     * Json5格式化、双引号包裹字符串、保持null元素（若允许多行文本，在编辑器会有展示问题）
     */
    public static final TnJsonBuilder FORMAT_DOUBLE_QUOTE_JSON5 = TnJson.builder().readable().formated().keepNull().allowComments()/*.allowMultiRowString()*/;

    /**
     * Json5压缩、保持null元素、单引号包裹字符串
     */
    public static final TnJsonBuilder COMPACT_JSON5 = TnJson.builder().readable().withoutKeyQuote().singleQuote().keepNull();


    /**
     * 判断是否为Json5
     *
     * @param text 文本
     * @return Json5为true，否则为false
     */
    public static boolean isJson5(String text) {
        Object data = tryResolveJson5(text);
        return data instanceof Map || data instanceof List;
    }


    /**
     * 判断是否为Json5数组
     *
     * @param text 文本
     * @return 若为数组，则返回 true，否则为 false
     */
    public static boolean isJson5Array(String text) {
        return tryResolveJson5(text) instanceof List;
    }


    /**
     * 判断是否为Json5对象
     *
     * @param text 文本
     * @return 若为对象，则返回 true，否则为 false
     */
    public static boolean isJson5Object(String text) {
        return tryResolveJson5(text) instanceof Map;
    }


    /**
     * 格式化Json5文本
     *
     * @param data 对象
     * @return 格式化后的Json5文本
     */
    public static String formatJson5(Object data) {
        return toJson5Str(data, FORMAT_JSON5);
    }

    /**
     * 格式化Json5文本（双引号）
     *
     * @param data 对象
     * @return 格式化后的Json5文本
     */
    public static String formatJson5WithDoubleQuote(Object data) {
        return toJson5Str(data, FORMAT_DOUBLE_QUOTE_JSON5);
    }

    /**
     * 格式化Json5文本
     *
     * @param json json字符串
     * @return 格式化后的Json5文本
     */
    public static String formatJson5(String json) {
        return toJson5Str(tryResolveJson5(json), FORMAT_JSON5);
    }

    /**
     * 格式化Json5文本
     *
     * @param json json字符串
     * @return 格式化后的Json5文本
     */
    public static String formatJson5WithComment(String json) {
        return toJson5Str(tryResolveJson5WithComment(json), FORMAT_JSON5);
    }

    /**
     * 将Json5压缩成一行
     *
     * @param json json字符串
     * @return 压缩Json5文本
     */
    public static String compressJson5(String json) {
        return toJson5Str(tryResolveJson5(json), COMPACT_JSON5);
    }

    public static String compressJson5(Object data) {
        return toJson5Str(data, COMPACT_JSON5);
    }

    /**
     * 解析Json5文本，若解析失败，返回null
     *
     * @param json json字符串
     * @return JsonWrapper包装对象，若解析失败，返回null
     */
    public static JsonWrapper parse(String json) {
        if (isJson5Object(json)) {
            return parseObject(json);
        } else if (isJson5Array(json)) {
            return parseArray(json);
        }
        return null;
    }

    /**
     * 解析Json5文本（注释），若解析失败，返回null
     *
     * @param json json字符串
     * @return JsonWrapper包装对象，若解析失败，返回null
     */
    public static JsonWrapper parseWithComment(String json) {
        if (isJson5Object(json)) {
            return parseObjectWithComment(json);
        } else if (isJson5Array(json)) {
            return parseArrayWithComment(json);
        }
        return null;
    }


    /**
     * 将Json5文本解析为ObjectWrapper（对象）
     *
     * @param text 文本
     * @return ObjectWrapper包装对象
     */
    public static ObjectWrapper parseObject(String text) {
        return new ObjectWrapper(resolveJson5(text, false));
    }

    /**
     * 将Json5文本解析为ObjectWrapper（对象）
     *
     * @param text 文本
     * @return ObjectWrapper包装对象
     */
    public static ObjectWrapper parseObjectWithComment(String text) {
        return new ObjectWrapper(resolveJson5(text, true));
    }


    /**
     * 将Json5文本解析为ArrayWrapper（数组）
     *
     * @param text 文本
     * @return ArrayWrapper包装对象
     */
    public static ArrayWrapper parseArray(String text) {
        return new ArrayWrapper(resolveJson5(text, false));
    }


    /**
     * 将Json5文本解析为ArrayWrapper（数组）
     *
     * @param text 文本
     * @return ArrayWrapper包装对象
     */
    public static ArrayWrapper parseArrayWithComment(String text) {
        return new ArrayWrapper(resolveJson5(text, true));
    }


    /**
     * 将Json5文本转换为Json文本
     *
     * @param json5Str Json5文本
     * @return Json文本
     */
    public static String convertJson5ToJson(String json5Str) {
        Object data = resolveJson5(json5Str, false);
        return Objects.isNull(data) ? null : JsonUtil.formatJson(data);
    }


    /**
     * 将Json文本转换为Json5文本
     *
     * @param jsonStr Json文本
     * @return Json5文本
     */
    public static String convertJsonToJson5(String jsonStr) {
        return toJson5Str(JsonUtil.parse(jsonStr));
    }


    /**
     * 将Json5对象转换为Json文本
     *
     * @param obj Json5对象
     * @return Json文本
     */
    public static String toJson5Str(Object obj) {
        return toJson5Str(obj, FORMAT_JSON5);
    }

    /**
     * 将Json5对象转换为Json文本
     *
     * @param obj Json5对象
     * @return Json文本
     */
    public static String toJson5StrWithDoubleQuote(Object obj) {
        return toJson5Str(obj, FORMAT_DOUBLE_QUOTE_JSON5);
    }


    /**
     * 将对象转换为Json文本
     *
     * @param data    对象
     * @param builder 构建器
     * @return Json文本
     */
    public static String toJson5Str(Object data, TnJsonBuilder builder) {
        try {
            String json5 = builder.buildJson(data);
            // 还原被转为unicode的字符
            return JsonAssistantUtil.unicodeToString(json5);
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
        return JsonUtil.isJson(text) ? null : resolveJson5(text, false);
    }

    /**
     * 解析Json5文本，若解析失败，返回null
     *
     * @param text 文本
     * @return 若为对象，则返回 Map；若为 List，则返回 List；否则返回 null
     */
    public static Object tryResolveJson5WithComment(String text) {
        // 判断是否为 Json，再判断是否为 Json5
        return JsonUtil.isJson(text) ? null : resolveJson5(text, true);
    }


    /**
     * 解析Json5文本，若解析失败，返回null
     *
     * @param text 文本
     * @return 若为对象，则返回 Map；若为 List，则返回 List；否则返回 null
     */
    @SuppressWarnings("deprecation")
    public static Object resolveJson5(String text, boolean parseComment) {
        if (StrUtil.isBlank(text)) return null;

        Map<String, Object> map = null;
        try {
            map = parseComment ? TnJson.parseWithComment(text) : TnJson.parse(text);
        } catch (Exception ignored) {
        }

        try {
            if (MapUtil.isEmpty(map)) {
                // 尝试转义
                text = StringEscapeUtils.unescapeJson(text);
                // 再次解析
                map = parseComment ? TnJson.parseWithComment(text) : TnJson.parse(text);
            }

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

    public static Map<?, ?> getCommentsMap(ObjectWrapper jsonObject) {
        Map<?, ?> commentsMap = null;
        Object commentsObj = jsonObject.get(PluginConstant.COMMENT_KEY);
        // 默认会使用 LinkedHashMap 作反序列化，但注释Map是 HashMap，判断一下，杜绝有同名的Key
        if (commentsObj instanceof HashMap && !(commentsObj instanceof LinkedHashMap)) {
            commentsMap = (Map<?, ?>) commentsObj;
        }

        return commentsMap;
    }


    public static String getComment(Map<?, ?> commentsMap, String key) {
        String comment = null;
        if (commentsMap != null) {
            Object commentObj = commentsMap.get(key);
            if (commentObj != null) {
                // 确保单行
                comment = commentObj.toString().replaceAll("[\r\n]+", " ");
            }
        }

        return comment;
    }


}
