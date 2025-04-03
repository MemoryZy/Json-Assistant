package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2023/12/4
 */
public enum LombokAnnotations {

    DATA("lombok.Data", "Data"),

    ACCESSORS("lombok.experimental.Accessors", "Accessors"),

    GETTER("lombok.Getter", "Getter"),

    SETTER("lombok.Setter", "Setter"),
    ;

    private final String value;
    private final String simpleName;

    LombokAnnotations(String value, String simpleName) {
        this.value = value;
        this.simpleName = simpleName;
    }

    public String getValue() {
        return value;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
