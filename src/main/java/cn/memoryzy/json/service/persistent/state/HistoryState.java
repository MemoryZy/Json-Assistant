package cn.memoryzy.json.service.persistent.state;

import cn.memoryzy.json.enums.HistoryDisplayMode;

/**
 * @author Memory
 * @since 2024/11/29
 */
@Deprecated
public class HistoryState {

    /**
     * 历史记录开关
     */
    public boolean switchHistory = true;

    /**
     * 历史记录显示类型
     */
    public HistoryDisplayMode historyViewType = HistoryDisplayMode.TREE;

    /**
     * 是否自动记录
     */
    public boolean autoStore = false;

}
