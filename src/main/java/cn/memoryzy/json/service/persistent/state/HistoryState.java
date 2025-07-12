package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.HistoryDisplayMode;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * 历史记录设置项
 *
 * @author Memory
 * @since 2024/11/29
 */
@Tag("history")
public class HistoryState {

    /**
     * 是否启用历史记录功能
     */
    private boolean enableHistory = true;

    /**
     * 历史记录显示类型
     */
    private HistoryDisplayMode historyDisplayMode = HistoryDisplayMode.TREE;

    /**
     * 是否自动记录
     */
    private boolean autoRecordHistory = false;


    public void setEnableHistory(boolean enableHistory) {
        this.enableHistory = enableHistory;
    }

    public void setHistoryDisplayMode(HistoryDisplayMode historyDisplayMode) {
        this.historyDisplayMode = historyDisplayMode;
    }

    public void setAutoRecordHistory(boolean autoRecordHistory) {
        this.autoRecordHistory = autoRecordHistory;
    }


    public boolean isEnableHistory() {
        return enableHistory;
    }

    public HistoryDisplayMode getHistoryDisplayMode() {
        return historyDisplayMode;
    }

    public boolean isAutoRecordHistory() {
        return autoRecordHistory;
    }
}