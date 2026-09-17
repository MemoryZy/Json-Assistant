package cn.memoryzy.json.enums;

import cn.hutool.core.util.StrUtil;

/**
 * @author Memory
 * @since 2025/6/17
 */
public enum DataFormatType {

    JSON("JSON"),

    JSON5("JSON5"),

    XML("XML"),

    YAML("YAML"),

    TYPE_SCRIPT("TYPE_SCRIPT"),

    TOML("TOML"),

    URL_PARAM("URL Param");

    private final String value;

    DataFormatType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DataFormatType ofValue(String value) {
        for (DataFormatType formatType : values()) {
            if (StrUtil.equalsIgnoreCase(formatType.value, value)) {
                return formatType;
            }
        }

        return null;
    }
}
