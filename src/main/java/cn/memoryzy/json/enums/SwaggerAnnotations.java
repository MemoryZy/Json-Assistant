package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2025/3/28
 */
public enum SwaggerAnnotations {

    API_MODEL_PROPERTY("io.swagger.annotations.ApiModelProperty", "ApiModelProperty"),

    /**
     * Swagger v3
     */
    SCHEMA("io.swagger.v3.oas.annotations.media.Schema", "Schema");


    private final String value;
    private final String simpleName;

    SwaggerAnnotations(String value, String simpleName) {
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
