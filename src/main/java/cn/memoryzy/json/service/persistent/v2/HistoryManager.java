package cn.memoryzy.json.service.persistent.v2;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.service.persistent.state.v2.JsonRecord;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Memory
 * @since 2025/6/18
 */
@State(name = "Json History", storages = {@Storage(value = JsonAssistantPlugin.STORAGE_HISTORY_FILE, roamingType = RoamingType.DISABLED)})
public class HistoryManager implements PersistentStateComponent<HistoryManager> {

    /**
     * 最大保留记录数
     */
    private static final int MAX_HISTORY_ITEMS = 50;

    public static HistoryManager getInstance(Project project) {
        return project.getService(HistoryManager.class);
    }


    /**
     * 历史记录列表（双向队列）
     */
    private Deque<JsonRecord> histories = new ArrayDeque<>(MAX_HISTORY_ITEMS);

    @Override
    public @Nullable HistoryManager getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HistoryManager state) {
        this.histories = state.histories;
        // 确保加载后不超出限制
        trimHistory();
    }

    /**
     * 添加新查询记录到历史
     *
     * @param record 查询记录对象
     */
    public synchronized void addEntry(JsonRecord record) {
        // 避免添加重复的连续记录
        if (!histories.isEmpty() && histories.peekLast().equals(record)) {
            return;
        }

        histories.addLast(record);
        trimHistory();
    }

    /**
     * 获取最近的历史记录（倒序：最新记录在前）
     *
     * @param maxItems 最多返回的记录数
     */
    public synchronized List<JsonRecord> getRecentHistory(int maxItems) {
        List<JsonRecord> recent = new ArrayList<>(maxItems);
        Iterator<JsonRecord> it = histories.descendingIterator();

        for (int i = 0; i < maxItems && it.hasNext(); i++) {
            recent.add(it.next());
        }

        return recent;
    }

    /**
     * 获取完整历史记录（正序：从旧到新）
     */
    public synchronized List<JsonRecord> getFullHistory() {
        return new ArrayList<>(histories);
    }

    /**
     * 清空所有历史记录
     */
    public synchronized void clearHistory() {
        histories.clear();
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

}
