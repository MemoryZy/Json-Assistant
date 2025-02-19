package cn.memoryzy.json.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.memoryzy.json.model.serializer.LocalDateTimeTypeHandler;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.project.Project;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 历史记录对象
 *
 * @author Memory
 * @since 2024/11/25
 */
public class HistoryEntry {

    public static final String insertTimeConstant = "insertTime";

    /**
     * 历史记录Id
     */
    private Integer id;

    /**
     * 记录名称
     */
    private String name;

    /**
     * 历史记录短文本（展示）
     */
    private String shortText;

    /**
     * 历史记录原文
     */
    private String jsonString;

    /**
     * 历史记录解析后的对象
     */
    private JsonWrapper jsonWrapper;

    /**
     * 历史记录插入时间
     */
    private LocalDateTime insertTime;


    // region 构造方法及Getter、Setter方法
    public HistoryEntry() {
    }

    public HistoryEntry(Integer id, JsonWrapper jsonWrapper) {
        this(id, jsonWrapper.toJsonString(), jsonWrapper);
    }

    public HistoryEntry(Integer id, String jsonString, JsonWrapper jsonWrapper) {
        this.id = id;
        this.shortText = getShortText(jsonWrapper);
        this.jsonString = jsonString;
        this.jsonWrapper = jsonWrapper;
        this.insertTime = LocalDateTime.now();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public void setJsonWrapper(JsonWrapper jsonWrapper) {
        this.jsonWrapper = jsonWrapper;
    }

    public void setInsertTime(LocalDateTime insertTime) {
        this.insertTime = insertTime;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortText() {
        return shortText;
    }

    public String getJsonString() {
        return jsonString;
    }

    public JsonWrapper getJsonWrapper() {
        return jsonWrapper;
    }

    public LocalDateTime getInsertTime() {
        return insertTime;
    }
    // endregion

    /**
     * 转为 Json5（供 JsonSerializer 的 addObj 方法调用）
     *
     * @return Json5
     */
    public String toJson() {
        Map<String, Object> map = BeanUtil.beanToMap(this);
        return Json5Util.toJson5Str(map, Json5Util.COMPACT_JSON5.handleType(new LocalDateTimeTypeHandler()));
    }

    public static HistoryEntry fromMap(Map<String, Object> map) {
        // 替换时间类型（加快转换速度）
        String insertTime = (String) map.get(insertTimeConstant);
        map.put(insertTimeConstant, LocalDateTimeUtil.parse(insertTime, DatePattern.NORM_DATETIME_PATTERN));
        return BeanUtil.toBean(map, HistoryEntry.class);
    }


    private static String getShortText(JsonWrapper jsonWrapper) {
        String jsonString = JsonUtil.compressJson(jsonWrapper);
        return JsonAssistantUtil.truncateText(Objects.requireNonNull(jsonString), 80, "...");
        // return StringUtil.convertLineSeparators(truncatedText, ContentChooser.RETURN_SYMBOL);
    }

    public static int calculateId(Project project) {
        JsonHistoryPersistentState persistentState2 = JsonHistoryPersistentState.getInstance(project);
        HistoryLimitedList historyList = persistentState2.getHistory();
        Integer id = historyList.stream().map(HistoryEntry::getId).max(Integer::compareTo).orElse(-1);
        return id + 1;
    }

}
