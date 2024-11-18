package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

/**
 * 窗口背景色调整策略
 *
 * @author Memory
 * @since 2024/11/1
 */
public enum BackgroundColorPolicy {
    DEFAULT("setting.component.background.color.item.default.text"),
    FOLLOW_MAIN_EDITOR("setting.component.background.color.item.follow.main.editor.text"),
    // CUSTOM("自定义")

    ;

    private final String key;

    BackgroundColorPolicy(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return JsonAssistantBundle.messageOnSystem(key);
    }
}
