package cn.memoryzy.json.service.persistent.state;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * 反序列化相关配置项
 *
 * @author Memory
 * @since 2025/6/17
 */
@Tag("deserialization")
public class DeserializationState {

    /**
     * 是否启用 FastJson 注解
     */
    private boolean enableFastJsonAnnotation = false;

    /**
     * 是否启用 FastJson2 注解
     */
    private boolean enableFastJson2Annotation = false;

    /**
     * 是否启用 Jackson 注解
     */
    private boolean enableJacksonAnnotation = false;

    /**
     * 类属性是否保持驼峰命名
     */
    private boolean keepFieldCamelCase = true;

    /**
     * 是否启用 Lombok @Data 注解
     */
    private boolean enableLombokData = true;

    /**
     * 是否启用 Lombok 链式访问器（@Accessors(chain = true)）
     */
    private boolean enableLombokChainAccessors = true;

    /**
     * 是否启用 Lombok @Getter 注解
     */
    private boolean enableLombokGetter = false;

    /**
     * 是否启用 Lombok @Setter 注解
     */
    private boolean enableLombokSetter = false;

    /**
     * 是否启用 Swagger 注解
     */
    private boolean enableSwaggerAnnotation = false;

    /**
     * 是否启用 Swagger 3.x 注解
     */
    private boolean enableSwagger3Annotation = false;

    /**
     * 是否启用属性类型推断
     */
    private boolean useInferType = true;


    public void setEnableFastJsonAnnotation(boolean enableFastJsonAnnotation) {
        this.enableFastJsonAnnotation = enableFastJsonAnnotation;
    }

    public void setEnableFastJson2Annotation(boolean enableFastJson2Annotation) {
        this.enableFastJson2Annotation = enableFastJson2Annotation;
    }

    public void setEnableJacksonAnnotation(boolean enableJacksonAnnotation) {
        this.enableJacksonAnnotation = enableJacksonAnnotation;
    }

    public void setKeepFieldCamelCase(boolean keepFieldCamelCase) {
        this.keepFieldCamelCase = keepFieldCamelCase;
    }

    public void setEnableLombokData(boolean enableLombokData) {
        this.enableLombokData = enableLombokData;
    }

    public void setEnableLombokChainAccessors(boolean enableLombokChainAccessors) {
        this.enableLombokChainAccessors = enableLombokChainAccessors;
    }

    public void setEnableLombokGetter(boolean enableLombokGetter) {
        this.enableLombokGetter = enableLombokGetter;
    }

    public void setEnableLombokSetter(boolean enableLombokSetter) {
        this.enableLombokSetter = enableLombokSetter;
    }

    public void setEnableSwaggerAnnotation(boolean enableSwaggerAnnotation) {
        this.enableSwaggerAnnotation = enableSwaggerAnnotation;
    }

    public void setEnableSwagger3Annotation(boolean enableSwagger3Annotation) {
        this.enableSwagger3Annotation = enableSwagger3Annotation;
    }

    public void setUseInferType(boolean useInferType) {
        this.useInferType = useInferType;
    }


    public boolean isEnableFastJsonAnnotation() {
        return enableFastJsonAnnotation;
    }

    public boolean isEnableFastJson2Annotation() {
        return enableFastJson2Annotation;
    }

    public boolean isEnableJacksonAnnotation() {
        return enableJacksonAnnotation;
    }

    public boolean isKeepFieldCamelCase() {
        return keepFieldCamelCase;
    }

    public boolean isEnableLombokData() {
        return enableLombokData;
    }

    public boolean isEnableLombokChainAccessors() {
        return enableLombokChainAccessors;
    }

    public boolean isEnableLombokGetter() {
        return enableLombokGetter;
    }

    public boolean isEnableLombokSetter() {
        return enableLombokSetter;
    }

    public boolean isEnableSwaggerAnnotation() {
        return enableSwaggerAnnotation;
    }

    public boolean isEnableSwagger3Annotation() {
        return enableSwagger3Annotation;
    }

    public boolean isUseInferType() {
        return useInferType;
    }
}
