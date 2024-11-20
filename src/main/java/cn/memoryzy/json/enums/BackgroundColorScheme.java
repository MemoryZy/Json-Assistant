package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.EditorAppearanceState;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/18
 */
public enum BackgroundColorScheme {

    /**
     * 跟随全局背景色
     */
    Default("setting.component.background.color.default.text", null),

    /**
     * 经典主题（黑白）
     */
    Classic("setting.component.background.color.classic.text", null),

    /**
     * 蓝色主题
     */
    Blue("setting.component.background.color.blue.text", JBColor.namedColor("FileColor.Blue", new JBColor(0xeaf6ff, 0x4f556b))),

    /**
     * 绿色主题
     */
    Green("setting.component.background.color.green.text", JBColor.namedColor("FileColor.Green", new JBColor(0xeffae7, 0x49544a))),

    /**
     * 橙色主题
     */
    Orange("setting.component.background.color.orange.text", JBColor.namedColor("FileColor.Orange", new JBColor(0xf6e9dc, 0x806052))),

    /**
     * 玫瑰色主题
     */
    Rose("setting.component.background.color.rose.text", JBColor.namedColor("FileColor.Rose", new JBColor(0xf2dcda, 0x6e535b))),

    /**
     * 紫罗兰色主题
     */
    Violet("setting.component.background.color.violet.text", JBColor.namedColor("FileColor.Violet", new JBColor(0xe6e0f1, 0x534a57))),

    /**
     * 黄色主题
     */
    Yellow("setting.component.background.color.yellow.text", JBColor.namedColor("FileColor.Yellow", new JBColor(0xffffe4, 0x4f4b41))),

    /**
     * 灰色主题
     */
    Gray("setting.component.background.color.gray.text", JBColor.namedColor("FileColor.Gray", new JBColor(0xf5f5f5, 0x45484a))),

    /**
     * 自定义主题
     */
    Custom("setting.component.background.color.custom.text", null);


    private final String key;
    private final Color color;

    BackgroundColorScheme(String key, Color color) {
        this.key = key;
        this.color = color;
    }

    public Color getColor() {
        if (Objects.equals(Default, this)) {
            return EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        } else if (Objects.equals(Classic, this)) {
            return PlatformUtil.isNewUi() ? new JBColor(0xffffff, 0x1e1f22) : new JBColor(0xffffff, 0x2b2b2b);
        } else if (Objects.equals(Custom, this)) {
            JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
            EditorAppearanceState appearanceState = persistentState.editorAppearanceState;
            return UIUtil.isUnderDarcula() ? appearanceState.customDarkcolor : appearanceState.customLightColor;
        }

        return color;
    }

    @Override
    public String toString() {
        return JsonAssistantBundle.messageOnSystem(key);
    }

}
