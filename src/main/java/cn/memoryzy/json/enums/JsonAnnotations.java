package cn.memoryzy.json.enums;

/**
 * Json注解
 *
 * @author Memory
 * @since 2023/11/27
 */
public enum JsonAnnotations {

    FAST_JSON_JSON_FIELD("com.alibaba.fastjson.annotation.JSONField", "JSONField"),
    FAST_JSON2_JSON_FIELD("com.alibaba.fastjson2.annotation.JSONField", "JSONField"),
    JACKSON_JSON_PROPERTY("com.fasterxml.jackson.annotation.JsonProperty", "JsonProperty"),
    JACKSON_JSON_IGNORE("com.fasterxml.jackson.annotation.JsonIgnore", "JsonIgnore"),
    JACKSON_JSON_FORMAT("com.fasterxml.jackson.annotation.JsonFormat", "JsonFormat");

    private final String value;
    private final String simpleName;

    JsonAnnotations(String value, String simpleName) {
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
