package cn.memoryzy.json.service.persistent.state;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * 序列化相关配置项
 *
 * @author Memory
 * @since 2025/6/17
 */
@Tag("serialization")
public class SerializationState {

    /**
     * 是否在序列化时包含随机值
     */
    private boolean serializeRandomValues = false;

    /**
     * 是否检测 FastJson 注解
     */
    private boolean detectFastJsonAnnotations = true;

    /**
     * 是否检测 Jackson 注解
     */
    private boolean detectJacksonAnnotations = true;


    public void setSerializeRandomValues(boolean serializeRandomValues) {
        this.serializeRandomValues = serializeRandomValues;
    }

    public void setDetectFastJsonAnnotations(boolean detectFastJsonAnnotations) {
        this.detectFastJsonAnnotations = detectFastJsonAnnotations;
    }

    public void setDetectJacksonAnnotations(boolean detectJacksonAnnotations) {
        this.detectJacksonAnnotations = detectJacksonAnnotations;
    }


    public boolean isSerializeRandomValues() {
        return serializeRandomValues;
    }

    public boolean isDetectFastJsonAnnotations() {
        return detectFastJsonAnnotations;
    }

    public boolean isDetectJacksonAnnotations() {
        return detectJacksonAnnotations;
    }
}
