package cn.memoryzy.json.enums;

import cn.hutool.core.util.StrUtil;

/**
 * @author Memory
 * @since 2024/12/19
 */
public enum JsonQueryLanguage {

    JSONPath,

    JMESPath;


    public static JsonQueryLanguage of(String name) {
        for (JsonQueryLanguage value : values()) {
            if (StrUtil.equalsIgnoreCase(value.name(), name)) {
                return value;
            }
        }

        return null;
    }

}
