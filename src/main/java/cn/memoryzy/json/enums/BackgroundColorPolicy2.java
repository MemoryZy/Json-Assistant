package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/18
 */
public enum BackgroundColorPolicy2 {
    DEFAULT("setting.component.background.color.item.default.text", null),
    White("", new JBColor(0xffffff, 0x1e1f22)),
    Blue("", JBColor.namedColor("FileColor.Blue", new JBColor(0xeaf6ff, 0x4f556b))),
    Green("", JBColor.namedColor("FileColor.Green", new JBColor(0xeffae7, 0x49544a))),
    Orange("", JBColor.namedColor("FileColor.Orange", new JBColor(0xf6e9dc, 0x806052))),
    Rose("", JBColor.namedColor("FileColor.Rose", new JBColor(0xf2dcda, 0x6e535b))),
    Violet("", JBColor.namedColor("FileColor.Violet", new JBColor(0xe6e0f1, 0x534a57))),
    Yellow("", JBColor.namedColor("FileColor.Yellow", new JBColor(0xffffe4, 0x4f4b41))),
    Gray("", JBColor.namedColor("FileColor.Gray", new JBColor(0xf5f5f5, 0x45484a)));

    private final String nameKey;
    private final Color color;

    BackgroundColorPolicy2(String nameKey, Color color) {
        this.nameKey = nameKey;
        this.color = color;
    }

    public Color getColor() {
        return Objects.equals(DEFAULT, this)
                // 获取全局背景色
                ? EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground()
                : color;
    }

    @Override
    public String toString() {
        return JsonAssistantBundle.messageOnSystem(nameKey);
    }
}
