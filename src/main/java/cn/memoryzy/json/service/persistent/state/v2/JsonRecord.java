package cn.memoryzy.json.service.persistent.state.v2;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.converter.JsonBase64Converter;
import cn.memoryzy.json.service.persistent.converter.JsonWrapperConverter;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * @author Memory
 * @since 2025/6/15
 */
@Tag("record")
@SuppressWarnings("UnusedReturnValue")
public class JsonRecord {

    /**
     * 记录Id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 记录短文本（展示）
     */
    private String displayText;

    /**
     * 记录原文
     */
    private String rawText;

    /**
     * 原文类型
     */
    private DataFormatType sourceType;

    /**
     * 记录解析后的 JSON 对象
     */
    private JsonWrapper wrapper;

    /**
     * 记录插入时间
     */
    private Long createTime;

    /**
     * 记录更新时间
     */
    private Long updateTime;

    /**
     * 原始数据长度 (字节数)
     */
    private Integer dataSize = 0;


    public JsonRecord() {
    }



    public JsonRecord setId(Integer id) {
        this.id = id;
        return this;
    }

    public JsonRecord setName(String name) {
        this.name = name;
        return this;
    }

    public JsonRecord setDisplayText(String displayText) {
        this.displayText = displayText;
        return this;
    }

    public JsonRecord setRawText(String rawText) {
        this.rawText = rawText;
        this.dataSize = StrUtil.isBlank(rawText) ? 0 : rawText.length();
        return this;
    }

    public JsonRecord setSourceType(DataFormatType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public JsonRecord setWrapper(JsonWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public JsonRecord setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public JsonRecord setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public JsonRecord setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
        return this;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @OptionTag(converter = JsonBase64Converter.class)
    public String getDisplayText() {
        return displayText;
    }

    @OptionTag(converter = JsonBase64Converter.class)
    public String getRawText() {
        return rawText;
    }

    public DataFormatType getSourceType() {
        return sourceType;
    }

    @OptionTag(converter = JsonWrapperConverter.class)
    public JsonWrapper getWrapper() {
        return wrapper;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public Integer getDataSize() {
        return dataSize;
    }
}
