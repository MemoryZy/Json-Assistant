package cn.memoryzy.json.enums;

/**
 * 窗口背景色调整策略
 *
 * @author Memory
 * @since 2024/11/1
 */
public enum BackgroundColorPolicy {
    DEFAULT("默认"),
    FOLLOW_MAIN_EDITOR("跟随主编辑器"),
    // CUSTOM("自定义")

    ;

    private final String name;

    BackgroundColorPolicy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
