package cn.memoryzy.json.model.template;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class AnnotationModel implements TemplateModel {

    private String name;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    @MapKey("ANNOTATION_STRING")
    private String annotationString;


    public AnnotationModel(String name) {
        this.name = name.startsWith("@") ? name.substring(1) : name;
        this.annotationString = toAnnotationString();
    }

    public AnnotationModel addAttribute(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            attributes.put(key, value);
        }
        this.annotationString = toAnnotationString();
        return this;
    }

    public static AnnotationModel withAttribute(String name, String attrName, Object attrValue) {
        return new AnnotationModel(name).addAttribute(attrName, attrValue);
    }

    public static AnnotationModel of(String name) {
        return new AnnotationModel(name);
    }

    /**
     * 判断是否有属性
     */
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    /**
     * 生成注解字符串（核心逻辑）
     */
    public String toAnnotationString() {
        StringBuilder builder = new StringBuilder("@").append(name);

        if (hasAttributes()) {
            builder.append("(");
            attributes.forEach((k, v) ->
                    builder.append(k)
                            .append(" = ")
                            .append(formatValue(v))
                            .append(", ")
            );
            // 移除最后逗号
            builder.setLength(builder.length() - 2);
            builder.append(")");
        }
        return builder.toString();
    }

    /**
     * 值格式化（处理字符串加引号）
     */
    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Character) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getAnnotationString() {
        return annotationString;
    }
}
