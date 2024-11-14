package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.deserialize.ArrayWrapper;
import cn.memoryzy.json.model.deserialize.JsonWrapper;
import cn.memoryzy.json.model.deserialize.ObjectWrapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonUtil {

    /**
     * 构建 JsonMapper（支持解析 '非数字NaN' 标识）
     */
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .disable(JsonWriteFeature.WRITE_NAN_AS_STRINGS)
            .build();

    /**
     * 检查字符串是否为JSON格式。
     *
     * @param text 待检查的字符串
     * @return 如果字符串是有效的JSON则返回true，否则返回false
     */
    public static boolean isJson(String text) {
        try {
            JsonNode node = MAPPER.readTree(text);
            return node instanceof ArrayNode || node instanceof ObjectNode;
        } catch (Throwable e) {
            return false;
        }
    }


    /**
     * 检查字符串是否为JSON数组。
     *
     * @param json 待检查的字符串
     * @return 如果字符串是有效的JSONArray则返回true，否则返回false
     */
    public static boolean isJsonArray(String json) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return jsonNode instanceof ArrayNode;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 检查字符串是否为JSON对象。
     *
     * @param json 待检查的字符串
     * @return 如果字符串是有效的JSONObject则返回true，否则返回false
     */
    public static boolean isJsonObject(String json) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return jsonNode instanceof ObjectNode;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将字符串解析为JsonWrapper包装对象
     *
     * @param json json字符串
     * @return JsonWrapper包装对象
     */
    public static JsonWrapper parse(String json) {
        if (isJsonObject(json)) {
            return parseObject(json);
        } else if (isJsonArray(json)) {
            return parseArray(json);
        }
        return null;
    }

    /**
     * 将字符串解析为ObjectWrapper包装对象（对象）
     *
     * @param text json字符串
     * @return ObjectWrapper包装对象
     */
    public static ObjectWrapper parseObject(String text) {
        return new ObjectWrapper(toObject(ensureJson(text), LinkedHashMap.class));
    }

    /**
     * 将字符串解析为ArrayWrapper包装对象（数组）
     *
     * @param text json字符串
     * @return ArrayWrapper包装对象
     */
    public static ArrayWrapper parseArray(String text) {
        return new ArrayWrapper(toObject(ensureJson(text), ArrayList.class));
    }


    /**
     * 格式化Json字符串
     *
     * @param jsonStr json字符串
     * @return 格式化后的json字符串
     */
    public static String formatJson(String jsonStr) {
        try {
            return formatJson(MAPPER.readTree(jsonStr));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 格式化Json字符串
     *
     * @param data 对象
     * @return 格式化后的json字符串
     */
    public static String formatJson(Object data) {
        try {
            return MAPPER.writer(new NoSpaceAndLFPrettyPrinter()).writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 将Json压缩成一行
     *
     * @param jsonStr json字符串
     * @return 压缩json
     */
    public static String compressJson(String jsonStr) {
        try {
            return MAPPER.writeValueAsString(MAPPER.readTree(jsonStr));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJsonStr(Object obj) {
        return formatJson(obj);
    }


    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonStr JSON字符串
     * @param clz     目标对象类型
     * @return 目标对象
     */
    public static <T> T toObject(String jsonStr, Class<T> clz) {
        try {
            return MAPPER.readValue(jsonStr, clz);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNotJsonArray(String jsonStr, boolean isJson) {
        return isJson ? (!JsonUtil.isJsonArray(jsonStr)) : (!Json5Util.isJson5Array(jsonStr));
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


    /**
     * 从给定的字符串中提取JSON字符串，使用正则表达式匹配
     *
     * @param includeJsonStr 包含JSON字符串的字符串
     * @return 提取的JSON字符串，如果给定的字符串为空或null，则返回空字符串
     */
    public static String extractJsonStringOnRegular(String includeJsonStr) {
        List<String> jsonStrings = findJsonStrings(includeJsonStr);
        return CollUtil.isNotEmpty(jsonStrings) ? jsonStrings.get(0) : "";
    }


    /**
     * 在给定的文本中查找所有可能的JSON字符串
     *
     * @param text 文本
     * @return 所有可能的JSON字符串的列表
     */
    @SuppressWarnings("RegExpRedundantEscape")
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


    /**
     * 使用默认的PrettyPrinter时，Key的后面总是会带一个空格，然后才是冒号，通过继承这个类做处理
     * <p>并且在Jackson生成的Json中换行符为系统默认的 \r\n 换行符，利用此类将其固定为 \n  <br/>
     * （{@link com.intellij.openapi.editor.Document} 类不允许编辑器内出现\r）</p>
     */
    private static class NoSpaceAndLFPrettyPrinter extends DefaultPrettyPrinter {

        public NoSpaceAndLFPrettyPrinter() {
            super();
            super._objectIndenter = new DefaultIndenter("  ", "\n");
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new NoSpaceAndLFPrettyPrinter();
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            // 将 "key" : "value" 变为 "key": "value"
            g.writeRaw(": ");
        }
    }


}
