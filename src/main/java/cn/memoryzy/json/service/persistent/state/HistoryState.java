package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.HistoryViewType;

/**
 * @author Memory
 * @since 2024/11/29
 */
public class HistoryState {

    /**
     * 历史记录开关
     */
    public boolean switchHistory = true;

    /**
     * 历史记录显示类型
     */
    public HistoryViewType historyViewType = HistoryViewType.TREE;

    /**
     * 是否自动记录
     */
    public boolean autoStore = false;

}
