package cn.memoryzy.json.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.deserialize.ObjectWrapper;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.util.Urls;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Memory
 * @since 2024/11/14
 */
public class DataConverter {

    public static boolean canPropertiesBeConvertedToJson(String properties) {
        if (JsonUtil.canResolveToJson(properties) || Json5Util.isJson5(properties)) {
            return false;
        }
        Map<String, Object> map = resolveProperties(properties);
        return MapUtil.isNotEmpty(map);
    }

    public static String propertiesToJson(String properties) {
        return new ObjectWrapper(resolveProperties(properties)).toString();
    }

    public static String jsonToProperties(String json, boolean isJson) {
        ObjectWrapper objectWrapper = isJson ? JsonUtil.parseObject(json) : Json5Util.parseObject(json);
        objectWrapper.entrySet().removeIf(entry -> shouldSkipValue(entry.getValue(), false));
        // 不转换为Properties对象，怕乱了顺序，还是用Map接收
        return mapToPropertiesFormat(objectWrapper);
    }

    public static Map<String, Object> resolveProperties(String property) {
        try (StringReader reader = new StringReader(property)) {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            Properties properties = new Properties();
            // 加载properties文本
            properties.load(reader);

            // 遍历Properties对象，检查并删除值为空字符串的键值对
            Enumeration<?> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = properties.getProperty(key);
                if (StrUtil.isNotBlank(value)) {
                    paramMap.put(key, value);
                }
            }

            return paramMap;
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * 将Map转换为 Properties 格式的字符串。
     *
     * @param map Map
     * @return properties格式的字符串
     */
    public static String mapToPropertiesFormat(Map<String, Object> map) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            // Properties以换行为区分
            value = value.replace("\n", "\\n");
            result.append(key).append("=").append(value).append("\n");
        }

        // 去掉最后一个多余的换行符
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }

    public static String urlParamsToJson(String url) {
        try {
            String query = parseQuery(url);
            if (StrUtil.isBlank(query)) {
                return null;
            }

            // 解析查询字符串
            Map<String, Object> params = new LinkedHashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int index = pair.indexOf("=");
                if (index > 0 && index < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);

                    // 判断是否为数字、时间、布尔类型
                    params.put(key, JsonAssistantUtil.detectType(value));
                }
            }

            // 转换为JSON
            return new ObjectWrapper(params).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseQuery(String urlStr) {
        try {
            // 尝试解析完整的URL
            return new java.net.URL(urlStr).getQuery();
        } catch (Exception e) {
            // 如果有多行，不符合条件
            if (urlStr.contains("\n")) {
                return null;
            }

            // 有多种可能，一种：?开头、没有?开头
            if (urlStr.charAt(0) == '?') {
                return urlStr;
            } else {
                if (urlStr.contains(" =") || urlStr.contains("= ")
                        || urlStr.contains(" &") || urlStr.contains("& ")) {
                    return null;
                }

                return urlStr;
            }
        }
    }


    public static String jsonToUrlParams(String json, boolean isJson) {
        // JsonMap中跳过Map、List、null、长文本String
        ObjectWrapper objectWrapper = isJson ? JsonUtil.parseObject(json) : Json5Util.parseObject(json);
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : objectWrapper.entrySet()) {
            Object value = entry.getValue();
            if (shouldSkipValue(value, true)) {
                continue;
            }

            params.put(entry.getKey(), value.toString());
        }

        String frontUrl = cn.memoryzy.json.constant.Urls.FRONT_URL;
        String external = Urls.newFromEncoded(frontUrl).addParameters(params).toExternalForm();
        return StringUtils.removeStart(external, frontUrl + "?");
    }


    /**
     * 检查值是否应该被跳过。
     *
     * @param value 要检查的值
     * @return 如果应该跳过，返回true；否则返回false
     */
    private static boolean shouldSkipValue(Object value, boolean checkLength) {
        if (Objects.isNull(value) || value instanceof Map || value instanceof List) {
            return true;
        }

        if (value instanceof String) {
            String str = (String) value;
            return StringUtils.isBlank(str) || (checkLength && str.length() > 500);
        }

        return false;
    }

    /**
     * 获取当前Json，判断是否非Json数组
     * <p style="color: blue;">Url Param、Toml、Properties 三种类型不支持Json数组的转换</p>
     *
     * @param dataContext 数据上下文
     * @return 如果是Json对象，则为true；如果是数组，则为false
     */
    public static boolean isNotJsonArray(DataContext dataContext) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(dataContext, context, PlatformUtil.getEditor(dataContext));
        return JsonUtil.isNotJsonArray(json, GlobalJsonConverter.isValidJson(context.getProcessor()));
    }


}
