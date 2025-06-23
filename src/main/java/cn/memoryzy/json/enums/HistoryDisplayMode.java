package cn.memoryzy.json.enums;

import cn.hutool.core.util.StrUtil;

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

    public static HistoryDisplayMode of(String name) {
        for (HistoryDisplayMode value : values()) {
            if (StrUtil.equalsIgnoreCase(value.name(), name)) {
                return value;
            }
        }

        return null;
    }
}
