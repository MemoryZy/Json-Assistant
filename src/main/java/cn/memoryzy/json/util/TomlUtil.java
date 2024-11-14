package cn.memoryzy.json.util;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.util.Map;

/**
 * @author Memory
 * @since 2024/9/24
 */
public class TomlUtil {

    public static boolean isToml(String tomlStr) {
        try {
            new Toml().read(tomlStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String toJson(String tomlStr) {
        Toml toml = new Toml().read(tomlStr);
        Map<String, Object> map = toml.toMap();
        return JsonUtil.toJsonStr(map);
    }

    public static String toToml(String jsonStr, boolean isJson) {
        Object jsonObject = isJson ? JsonUtil.parseObject(jsonStr) : Json5Util.parseObject(jsonStr);
        // 单纯的 List 类型无法转换
        return new TomlWriter().write(jsonObject);
    }

}
