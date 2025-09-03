package cn.memoryzy.json.service.persistent.state;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.converter.Base64Converter;
import cn.memoryzy.json.service.persistent.converter.CompressConverter;
import cn.memoryzy.json.service.persistent.converter.JsonWrapperConverter;
import cn.memoryzy.json.ui.tree.BaseNode;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/6/15
 */
@Tag("record")
@SuppressWarnings("UnusedReturnValue")
public class JsonRecord extends BaseNode {

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

    @OptionTag(converter = Base64Converter.class)
    public String getName() {
        return name;
    }

    @OptionTag(converter = CompressConverter.class)
    public String getDisplayText() {
        return displayText;
    }

    @OptionTag(converter = CompressConverter.class)
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRecord record = (JsonRecord) o;
        return Objects.equals(id, record.id) && Objects.equals(name, record.name) && Objects.equals(displayText, record.displayText) && Objects.equals(rawText, record.rawText) && sourceType == record.sourceType && Objects.equals(wrapper, record.wrapper) && Objects.equals(createTime, record.createTime) && Objects.equals(updateTime, record.updateTime) && Objects.equals(dataSize, record.dataSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayText, rawText, sourceType, wrapper, createTime, updateTime, dataSize);
    }
}
