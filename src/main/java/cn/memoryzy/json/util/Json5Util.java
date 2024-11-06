package cn.memoryzy.json.util;

import a2u.tn.utils.json.TnJson;
import cn.hutool.core.map.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * Json5 处理
 *
 * @author Memory
 * @since 2024/11/6
 */
public class Json5Util {

    public static boolean isJson5(String text) {
        Object data = resolveJson5(text);
        return data instanceof Map || data instanceof List;
    }

    public static boolean isJson5Array(String text) {
        return resolveJson5(text) instanceof List;
    }

    public static boolean isJson5Object(String text) {
        return resolveJson5(text) instanceof Map;
    }


    public static String formatJson5(String json) {
        return TnJson.toJson(resolveJson5(json), TnJson.Mode.JSON5);
    }

    /**
     * 将Json压缩成一行
     *
     * @param json json字符串
     * @return 压缩Json
     */
    public static String compressJson5(String json) {
        return TnJson.toJson(resolveJson5(json), TnJson.Mode.JSON5COMPACT);
    }


    /**
     * 解析Json5文本，若解析失败，返回null
     *
     * @param text 文本
     * @return 若为对象，则返回 Map；若为 List，则返回 List；否则返回 null
     */
    public static Object resolveJson5(String text) {
        try {
            // 判断是否为 Json，再判断是否为 Json5
            if (JsonUtil.isJson(text)) {
                return null;
            }

            Map<String, Object> map = TnJson.parse(text);
            if (MapUtil.isNotEmpty(map)) {
                // 若是 Array，那么 Map 中只会存在一个 key，且 key 为 list
                if (map.size() == 1) {
                    Object list = map.get(TnJson.DEFAULT_LIST_KEY);
                    if (list instanceof List) {
                        return list;
                    }
                }

                // 不为 List，表示为 Json5 Object
                return map;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

}
