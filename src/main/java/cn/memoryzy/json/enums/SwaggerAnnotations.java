package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2025/3/28
 */
public enum SwaggerAnnotations {

    API_MODEL_PROPERTY("io.swagger.annotations.ApiModelProperty"),

    /**
     * Swagger v3
     */
    SCHEMA("io.swagger.v3.oas.annotations.media.Schema");


    private final String value;

    SwaggerAnnotations(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
