package cn.memoryzy.json.service.persistent.state;

/**
 * 反序列化设置项
 *
 * @author Memory
 * @since 2024/12/23
 */
public class DeserializerState {

    /**
     * 添加 FastJson 注解
     */
    public boolean fastJsonAnnotation = false;

    /**
     * 添加 FastJson2 注解
     */
    public boolean fastJson2Annotation = false;

    /**
     * 添加 Jackson 注解
     */
    public boolean jacksonAnnotation = false;

    /**
     * 类属性保持驼峰命名
     */
    public boolean keepCamelCase = true;

    /**
     * 添加 Lombok @Data 注解
     */
    public boolean dataLombokAnnotation = true;

    /**
     * 添加 Lombok @Accessors(chain = true) 注解
     */
    public boolean accessorsChainLombokAnnotation = true;

    /**
     * 添加 Lombok @Getter 注解
     */
    public boolean getterLombokAnnotation = false;

    /**
     * 添加 Lombok @Setter 注解
     */
    public boolean setterLombokAnnotation = false;

    /**
     * 添加 Swagger 字段描述注解
     */
    public boolean swaggerAnnotation = false;

    /**
     * 添加 Swagger v3 字段描述注解
     */
    public boolean swaggerV3Annotation = false;

}
