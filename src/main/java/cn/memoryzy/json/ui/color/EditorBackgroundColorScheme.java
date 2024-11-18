package cn.memoryzy.json.ui.color;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.DelegateColorScheme;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;

/**
 * @author Memory
 * @since 2024/11/18
 */
public class EditorBackgroundColorScheme extends DelegateColorScheme {

    private final JBColor background;

    public static final String whiteColor = "White";
    public static final String blueColor = "Blue";
    public static final String greenColor = "Green";
    public static final String orangeColor = "Orange";
    public static final String roseColor = "Rose";
    public static final String violetColor = "Violet";
    public static final String yellowColor = "Yellow";
    public static final String grayColor = "Gray";
    public static final String customColor = "Custom";

    public static final Map<String, Color> ourDefaultColors = Map.of(
            whiteColor, new JBColor(0xffffff, 0x1e1f22),
            blueColor, JBColor.namedColor("FileColor.Blue", new JBColor(0xeaf6ff, 0x4f556b)),
            greenColor, JBColor.namedColor("FileColor.Green", new JBColor(0xeffae7, 0x49544a)),
            orangeColor, JBColor.namedColor("FileColor.Orange", new JBColor(0xf6e9dc, 0x806052)),
            roseColor, JBColor.namedColor("FileColor.Rose", new JBColor(0xf2dcda, 0x6e535b)),
            violetColor, JBColor.namedColor("FileColor.Violet", new JBColor(0xe6e0f1, 0x534a57)),
            yellowColor, JBColor.namedColor("FileColor.Yellow", new JBColor(0xffffe4, 0x4f4b41)),
            grayColor, JBColor.namedColor("FileColor.Gray", new JBColor(0xf5f5f5, 0x45484a))
    );

    // 有亮色主题的自定义颜色、暗色主题的自定义颜色

    public EditorBackgroundColorScheme(@NotNull EditorColorsScheme delegate, final JBColor background) {
        super(delegate);
        this.background = background;
        // 如果开启了演示模式
        if (UISettings.getInstance().getPresentationMode()) {
            // 设置为演示模式的字体大小
            delegate.setEditorFontSize(UISettings.getInstance().getPresentationModeFontSize());
        }
    }

    @Override
    public @NotNull Color getDefaultBackground() {
        return background;
    }

}
