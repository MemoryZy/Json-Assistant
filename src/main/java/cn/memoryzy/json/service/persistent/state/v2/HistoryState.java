package cn.memoryzy.json.service.persistent.state.v2;

import cn.memoryzy.json.enums.HistoryDisplayMode;

/**
 * 历史记录设置项
 *
 * @author Memory
 * @since 2024/11/29
 */
public class HistoryState {

    /**
     * 是否启用历史记录功能
     */
    private boolean historyEnabled = true;

    /**
     * 历史记录显示类型
     */
    private HistoryDisplayMode historyDisplayMode = HistoryDisplayMode.TREE;

    /**
     * 是否自动记录
     */
    private boolean autoRecordHistory = false;


    public void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }

    public void setHistoryDisplayMode(HistoryDisplayMode historyDisplayMode) {
        this.historyDisplayMode = historyDisplayMode;
    }

    public void setAutoRecordHistory(boolean autoRecordHistory) {
        this.autoRecordHistory = autoRecordHistory;
    }


    public boolean isHistoryEnabled() {
        return historyEnabled;
    }

    public HistoryDisplayMode getHistoryDisplayMode() {
        return historyDisplayMode;
    }

    public boolean isAutoRecordHistory() {
        return autoRecordHistory;
    }
}