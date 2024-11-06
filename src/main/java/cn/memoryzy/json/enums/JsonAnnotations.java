package cn.memoryzy.json.enums;

/**
 * Json注解
 *
 * @author Memory
 * @since 2023/11/27
 */
public enum JsonAnnotations {

    FAST_JSON_JSON_FIELD("com.alibaba.fastjson.annotation.JSONField"),
    FAST_JSON2_JSON_FIELD("com.alibaba.fastjson2.annotation.JSONField"),
    JACKSON_JSON_PROPERTY("com.fasterxml.jackson.annotation.JsonProperty"),
    JACKSON_JSON_IGNORE("com.fasterxml.jackson.annotation.JsonIgnore"),
    JACKSON_JSON_FORMAT("com.fasterxml.jackson.annotation.JsonFormat");

    private final String value;

    JsonAnnotations(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}