package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2024/11/29
 */
public enum HistoryDisplayMode {

    /**
     * 树形视图
     */
    TREE("setting.component.history.tree.text"),

    /**
     * 列表视图
     */
    LIST("setting.component.history.list.text");

    private final String key;

    HistoryDisplayMode(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
