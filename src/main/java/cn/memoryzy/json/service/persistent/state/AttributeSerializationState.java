package cn.memoryzy.json.service.persistent.state;

/**
 * 属性序列化
 *
 * @author Memory
 * @since 2024/11/18
 */
public class AttributeSerializationState {

    /**
     * 是否包含随机值（属性序列化为 JSON 时）
     */
    public boolean includeRandomValues = false;

    /**
     * 识别 FastJson 注解
     */
    public boolean recognitionFastJsonAnnotation = true;

    /**
     * 识别 Jackson 注解
     */
    public boolean recognitionJacksonAnnotation = true;

}
