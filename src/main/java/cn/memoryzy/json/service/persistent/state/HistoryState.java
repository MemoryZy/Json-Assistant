package cn.memoryzy.json.service.persistent.state;

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
     * 是否自动记录
     */
    private boolean autoRecordHistory = false;


    public void setEnableHistory(boolean enableHistory) {
        this.enableHistory = enableHistory;
    }

    public void setAutoRecordHistory(boolean autoRecordHistory) {
        this.autoRecordHistory = autoRecordHistory;
    }


    public boolean isEnableHistory() {
        return enableHistory;
    }

    public boolean isAutoRecordHistory() {
        return autoRecordHistory;
    }
}