package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static final JSONConfig HUTOOL_JSON_CONFIG = JSONConfig.create().setStripTrailingZeros(false).setIgnoreNullValue(false);

    public static boolean isJson(String text) {
        try {
            JSONUtil.parse(text);
            JsonNode jsonNode = MAPPER.readTree(text);
            return jsonNode instanceof ArrayNode || jsonNode instanceof ObjectNode;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean isJsonArray(String json) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return jsonNode instanceof ArrayNode;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static boolean isJsonObject(String json) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return jsonNode instanceof ObjectNode;
        } catch (JsonProcessingException e) {
            return false;
        }
    }


    public static String formatJson(String jsonStr) {
        JSON json = JSONUtil.parse(jsonStr, HUTOOL_JSON_CONFIG);
        return json.toJSONString(2);
    }

    /**
     * 将Json压缩成一行
     *
     * @param jsonStr json字符串
     * @return 压缩json
     * @throws JsonProcessingException 非法Json
     */
    public static String compressJson(String jsonStr) throws JsonProcessingException {
        return MAPPER.writeValueAsString(MAPPER.readTree(jsonStr));
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static Object toBean(String jsonStr) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonStr);
            return MAPPER.convertValue(jsonNode, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


    /**
     * 判断文本是否为 JSON，如果不是，那就尝试从文本中提取 JSON
     * <p>如果给定的文本或提取的文本不是有效的 JSON，则返回 null</p>
     *
     * @param text 文本
     * @return JSON字符串
     */
    public static String ensureJson(String text) {
        return isJson(text) ? text : extractJson(text);
    }


    /**
     * 判断是否能解析为 JSON
     *
     * @param text 文本
     * @return 解析成功为 true，反之为 false
     */
    public static boolean canResolveToJson(String text) {
        return isJson(text) || StrUtil.isNotBlank(extractJson(text));
    }


    /**
     * 从给定的字符串中提取JSON字符串
     *
     * @param includeJsonStr 包含JSON字符串的字符串
     * @return 提取的JSON字符串，如果给定的字符串为空或null，则返回空字符串
     */
    @SuppressWarnings("deprecation")
    public static String extractJson(String includeJsonStr) {
        if (StrUtil.isBlank(includeJsonStr)) {
            return "";
        }

        try {
            // 转义判断
            String json = StringEscapeUtils.unescapeJson(includeJsonStr);
            if (isJson(json)) {
                return json;
            }

            json = extractJsonStringOnRegular(includeJsonStr);
            if (StrUtil.isNotBlank(json)) {
                return json;
            }

            json = extractJsonString(includeJsonStr);
            // 判断是否是JSON字符串
            return isJson(json) ? json : "";
        } catch (Exception e) {
            return "";
        }
    }


    public static String extractJsonStringOnRegular(String includeJsonStr) {
        List<String> jsonStrings = findJsonStrings(includeJsonStr);
        return CollUtil.isNotEmpty(jsonStrings) ? jsonStrings.get(0) : "";
    }


    public static List<String> findJsonStrings(String text) {
        List<String> jsonStrings = new ArrayList<>();

        // 注意：这个正则表达式是宽松的，并且可能无法捕获所有有效或无效的JSON字符串
        // 它尝试匹配以{或[开头，并且以}或]结尾的字符串，但会忽略内部的复杂性
        String jsonPattern = "\\{[^\\}]+\\}|\\[[^\\]]+\\]";

        Pattern pattern = Pattern.compile(jsonPattern);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // 捕获整个匹配项
            String jsonString = matcher.group();
            if (isJson(jsonString)) {
                jsonStrings.add(StrUtil.trim(jsonString));
            }
        }

        return jsonStrings;
    }


    /**
     * 从给定的字符串中提取JSON字符串
     *
     * @param includeJsonStr 包含JSON字符串的字符串
     * @return 提取的JSON字符串，如果给定的字符串为空或null，则返回空字符串
     */
    private static String extractJsonString(String includeJsonStr) {
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < includeJsonStr.length(); i++) {
            if (includeJsonStr.charAt(i) == '{' || includeJsonStr.charAt(i) == '[') {
                startIndex = i;
                break;
            }
        }
        if (startIndex == -1) {
            return ""; // 没有找到 JSON 字符串
        }

        int count = 1; // 计数器，用于匹配 {} 或 []
        for (int i = startIndex + 1; i < includeJsonStr.length(); i++) {
            char c = includeJsonStr.charAt(i);
            if (c == '{' || c == '[') {
                count++;
            } else if (c == '}' || c == ']') {
                count--;
                if (count == 0) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (endIndex == -1) {
            return ""; // JSON 字符串不完整
        }

        return includeJsonStr.substring(startIndex, endIndex + 1);
    }

}
