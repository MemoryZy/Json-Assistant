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
     * 添加 Jackson 注解
     */
    public boolean jacksonAnnotation = false;

    /**
     * 类属性保持驼峰命名
     */
    public boolean keepCamelCase = true;

}
