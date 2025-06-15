package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.model.wrapper.JsonWrapper;

/**
 * @author Memory
 * @since 2025/6/15
 */
public class JsonRecord {

    /**
     * 记录Id
     */
    public Integer id;

    /**
     * 名称
     */
    public String name;

    /**
     * 记录短文本（展示）
     */
    public String displayText;

    /**
     * 记录原文
     */
    public String rawText;

    /**
     * 原文类型
     */
    public String sourceType;

    /**
     * 记录解析后的 JSON 对象
     */
    public JsonWrapper jsonWrapper;

    /**
     * 记录插入时间
     */
    public Long createTime;

    /**
     * 记录更新时间
     */
    private Long updateTime;

    /**
     * 原始数据长度 (字节数)
     */
    private int dataSize;



}
