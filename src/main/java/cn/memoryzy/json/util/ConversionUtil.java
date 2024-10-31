package cn.memoryzy.json.util;

import cn.hutool.json.JSONObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class ConversionUtil {

    public static String urlParamsToJson(String url) {
        try {
            // 去掉URL中的协议、主机名等部分，只保留查询字符串
            String query = new java.net.URL(url).getQuery();
            if (query == null || query.isEmpty()) {
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
                    params.put(key, value);
                }
            }

            // 转换为JSON
            return new JSONObject(params).toJSONString(2);
        } catch (Exception e) {
            // LOG.error("Error parsing URL parameters", e);
            return null;
        }
    }

}
