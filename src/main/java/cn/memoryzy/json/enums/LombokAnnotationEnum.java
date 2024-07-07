package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2023/12/4
 */
public enum LombokAnnotationEnum {

    DATA("lombok.Data"),

    ACCESSORS("lombok.experimental.Accessors");

    private final String value;

    LombokAnnotationEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
