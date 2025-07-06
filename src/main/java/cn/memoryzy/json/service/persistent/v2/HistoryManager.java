package cn.memoryzy.json.service.persistent.v2;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/6/18
 */
@Service(Service.Level.PROJECT)
@State(name = "Json History", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_HISTORY_FILE, roamingType = RoamingType.DISABLED)})
public final class HistoryManager implements PersistentStateComponent<HistoryManager> {

    /**
     * 最大保留记录数
     */
    private static final int MAX_HISTORY_ITEMS = 30;

    public static HistoryManager getInstance(Project project) {
        return project.getService(HistoryManager.class);
    }

    /**
     * 配置版本
     */
    private Integer version = JsonAssistantPlugin.CONFIG_VERSION;

    /**
     * 历史记录列表（双向队列）
     */
    private Deque<JsonRecord> histories = new ArrayDeque<>(MAX_HISTORY_ITEMS);

    @Override
    public @NotNull HistoryManager getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HistoryManager state) {
        this.histories = state.histories;
        // 确保加载后不超出限制
        trimHistory();
    }


    /**
     * 添加新记录到历史
     *
     * @param record 记录对象
     */
    public synchronized JsonRecord addEntry(JsonRecord record) {
        JsonWrapper wrapper = record.getWrapper();
        // 避免添加重复记录
        if (!exists(wrapper)) {
            // 计算并补充属性
            populateMissingAttributes(record);
            // 添加
            histories.addLast(record);
            // 裁剪
            trimHistory();
        }

        return record;
    }


    /**
     * 获取最近的历史记录（倒序：最新记录在前）
     */
    public synchronized List<JsonRecord> getRecentHistories() {
        // 将历史记录拷贝到临时列表（避免直接操作原始数据）
        List<JsonRecord> recent = new ArrayList<>(histories);
        recent.sort(Comparator.comparingLong(JsonRecord::getUpdateTime).reversed());
        return recent;
    }

    /**
     * 将历史记录以更新时间进行分组
     *
     * @return 分组结果
     */
    public synchronized Map<String, List<JsonRecord>> groupByUpdateTime() {
        return histories.stream().collect(Collectors.groupingBy(record -> {
            String timeStr = DateUtil.format(new Date(record.getUpdateTime()), DatePattern.NORM_DATE_FORMATTER);
            return timeStr != null ? timeStr : PluginConstant.UNKNOWN;
        }));
    }

    /**
     * 清空所有历史记录
     */
    public synchronized void clearHistory() {
        histories.clear();
    }

    /**
     * 判断是否已经存在
     *
     * @param wrapper 要添加的元素
     * @return 存在则返回true，不存在则返回false
     */
    public boolean exists(JsonWrapper wrapper) {
        return histories.stream().anyMatch(record -> Objects.equals(wrapper, record.getWrapper()));
    }


    /**
     * 查找相同结构的记录
     *
     * @param wrapper 结构
     * @return 记录
     */
    public JsonRecord find(JsonWrapper wrapper) {
        return histories.stream().filter(record -> Objects.equals(wrapper, record.getWrapper())).findFirst().orElse(null);
    }

    /**
     * 查找相同名称的记录
     *
     * @param name 名称
     * @return 记录
     */
    public JsonRecord findByName(String name) {
        return histories.stream()
                .filter(record -> StrUtil.isNotBlank(record.getName()) && Objects.equals(name, record.getName()))
                .findFirst()
                .orElse(null);
    }

    public void batchRemove(List<Integer> ids) {
        histories.removeIf(record -> ids.contains(record.getId()));
    }

    public void batchRemove(Integer... ids) {
        histories.removeIf(record -> List.of(ids).contains(record.getId()));
    }

    /**
     * 裁剪历史记录到最大容量
     */
    private void trimHistory() {
        while (histories.size() > MAX_HISTORY_ITEMS) {
            // FIFO策略：移除最早记录
            histories.removeFirst();
        }
    }

    /**
     * 补充记录属性
     *
     * @param record 记录
     */
    private void populateMissingAttributes(JsonRecord record) {
        if (null == record.getId()) {
            record.setId(histories.stream().map(JsonRecord::getId).max(Integer::compareTo).orElse(-1) + 1);
        }

        if (StrUtil.isBlank(record.getDisplayText())) {
            record.setDisplayText(getShortText(record.getWrapper()));
        }

        Long createTime = record.getCreateTime();
        if (null == createTime || createTime <= 0) {
            record.setCreateTime(System.currentTimeMillis());
        }

        Long updateTime = record.getUpdateTime();
        if (null == updateTime || updateTime <= 0) {
            record.setUpdateTime(System.currentTimeMillis());
        }
    }

    public static String getShortText(JsonWrapper wrapper) {
        String jsonString = JsonUtil.compressJson(wrapper);
        return JsonAssistantUtil.truncateText(Objects.requireNonNull(jsonString), 80, "...");
        // return StringUtil.convertLineSeparators(truncatedText, ContentChooser.RETURN_SYMBOL);
    }


    @Attribute
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Deque<JsonRecord> getHistories() {
        return histories;
    }

    public void setHistories(Deque<JsonRecord> histories) {
        this.histories = histories;
    }
}
