package cn.memoryzy.json.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static boolean isJsonStr(String text) {
        try {
            MAPPER.readTree(text);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }


    public static String formatJson(String jsonStr) {
        JSON json = JSONUtil.parse(jsonStr);
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

    /**
     * 从给定的字符串中提取JSON字符串
     *
     * @param includeJsonStr 包含JSON字符串的字符串
     * @return 提取的JSON字符串，如果给定的字符串为空或null，则返回空字符串
     */
    public static String extractJsonStr(String includeJsonStr) {
        if (StrUtil.isBlank(includeJsonStr)) {
            return "";
        }

        String json = extractJsonString(includeJsonStr);
        // 判断是否是JSON字符串
        return isJsonStr(json) ? json : "";
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
