package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2023/12/4
 */
public enum LombokAnnotations {

    DATA("lombok.Data"),

    ACCESSORS("lombok.experimental.Accessors");

    private final String value;

    LombokAnnotations(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
