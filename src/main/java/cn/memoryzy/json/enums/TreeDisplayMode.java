package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

/**
 * @author Memory
 * @since 2024/12/11
 */
public enum TreeDisplayMode {

    /**
     * 弹出窗口
     */
    POPUP("setting.component.tree.display.popup.mode.text"),

    /**
     * 原本的工具窗口窗口（Json Assistant）
     */
    ORIGINAL_TOOLWINDOW("setting.component.tree.display.original.toolwindow.mode.text"),

    /**
     * 辅助侧边栏窗口（动态注册的窗口）
     */
    AUXILIARY_TOOLWINDOW("setting.component.tree.display.new.toolwindow.mode.text");


    private final String key;

    TreeDisplayMode(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return JsonAssistantBundle.messageOnSystem(key);
    }
}
