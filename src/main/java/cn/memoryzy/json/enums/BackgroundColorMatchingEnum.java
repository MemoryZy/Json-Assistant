package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2024/11/1
 */
public enum BackgroundColorMatchingEnum {
    DEFAULT("默认"),
    FOLLOW_MAIN_EDITOR("跟随主编辑器"),
    // CUSTOM("自定义")

    ;

    private final String name;

    BackgroundColorMatchingEnum(String name) {
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
