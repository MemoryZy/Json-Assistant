package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.converter.JsonWrapperConverter;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * @author Memory
 * @since 2025/6/15
 */
@Tag("record")
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
    private String sourceType;

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
    private Integer dataSize;


    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setWrapper(JsonWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getRawText() {
        return rawText;
    }

    public String getSourceType() {
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
