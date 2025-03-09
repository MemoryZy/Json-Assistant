package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

/**
 * @author Memory
 * @since 2024/11/29
 */
public enum HistoryViewType {

    /**
     * 树形视图
     */
    TREE("setting.component.history.tree.text"),

    /**
     * 列表视图
     */
    LIST("setting.component.history.list.text");

    private final String key;

    HistoryViewType(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return JsonAssistantBundle.messageOnSystem(key);
    }
}
