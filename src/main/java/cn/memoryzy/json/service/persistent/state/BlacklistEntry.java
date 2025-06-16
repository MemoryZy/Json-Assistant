package cn.memoryzy.json.service.persistent.state;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.editor.actions.ContentChooser;
import com.intellij.openapi.util.text.StringUtil;

import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/3/4
 */
public class BlacklistEntry {

    public static final String insertTimeConstant = "insertTime";

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
    public String shortText;

    /**
     * 记录原文
     */
    public String originalText;

    /**
     * 原文类型
     */
    public String originalDataType;

    /**
     * 记录解析后的 JSON 对象
     */
    public JsonWrapper jsonWrapper;

    /**
     * 历史记录插入时间
     */
    public Long insertTime;

    public BlacklistEntry() {
    }

    public BlacklistEntry(Integer id, String originalText, String originalDataType, JsonWrapper jsonWrapper) {
        this.id = id;
        this.originalText = originalText;
        this.originalDataType = originalDataType;
        this.shortText = getShortText(originalText, originalDataType, jsonWrapper);
        this.jsonWrapper = jsonWrapper;
        this.insertTime = System.currentTimeMillis();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getOriginalDataType() {
        return originalDataType;
    }

    public void setOriginalDataType(String originalDataType) {
        this.originalDataType = originalDataType;
    }

    public JsonWrapper getJsonWrapper() {
        return jsonWrapper;
    }

    public void setJsonWrapper(JsonWrapper jsonWrapper) {
        this.jsonWrapper = jsonWrapper;
    }

    public Long getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Long insertTime) {
        this.insertTime = insertTime;
    }

    public static BlacklistEntry fromMap(Map<String, Object> map) {
        // 替换时间类型（加快转换速度）
        String insertTime = (String) map.get(insertTimeConstant);
        map.put(insertTimeConstant, DateUtil.parse(insertTime, DatePattern.NORM_DATETIME_PATTERN));
        return BeanUtil.toBean(map, BlacklistEntry.class);
    }

    private static String getShortText(String originalText, String originalDataType, JsonWrapper jsonWrapper) {
        String text;
        if (DataTypeConstant.JSON.equals(originalDataType)) {
            text = JsonUtil.compressJson(jsonWrapper);
        } else if (DataTypeConstant.JSON5.equals(originalDataType)){
            text = Json5Util.compressJson5(jsonWrapper);
        } else {
            text = originalText;
        }

        String result = JsonAssistantUtil.truncateText(Objects.requireNonNull(text), 80, "...");
        return StringUtil.convertLineSeparators(result, ContentChooser.RETURN_SYMBOL);
    }
}
