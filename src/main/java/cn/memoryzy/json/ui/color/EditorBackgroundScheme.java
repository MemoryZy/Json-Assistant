package cn.memoryzy.json.ui.color;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.DelegateColorScheme;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/11/18
 */
public class EditorBackgroundScheme extends DelegateColorScheme {

    private final Color background;

    public EditorBackgroundScheme(@NotNull EditorColorsScheme delegate, final Color background) {
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
