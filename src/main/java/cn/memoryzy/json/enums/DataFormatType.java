package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2025/6/17
 */
public enum DataFormatType {

    XML("XML"),

    YAML("YAML"),

    TOML("TOML"),

    URL_PARAM("URL Param");

    private final String value;

    DataFormatType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
