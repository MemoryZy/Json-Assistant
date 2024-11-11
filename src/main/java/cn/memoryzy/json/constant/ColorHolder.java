package cn.memoryzy.json.constant;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/11/10
 */
public interface ColorHolder {

    interface Foreground {
        @NotNull Color DISABLED = JBColor.namedColor("Link.disabledForeground", JBUI.CurrentTheme.Label.disabledForeground());
        @NotNull Color ENABLED = JBColor.namedColor("Link.activeForeground", JBColor.namedColor("link.foreground", 0x589DF6));
        @NotNull Color HOVERED = JBColor.namedColor("Link.hoverForeground", JBColor.namedColor("link.hover.foreground", ENABLED));
        @NotNull Color PRESSED = JBColor.namedColor("Link.pressedForeground", JBColor.namedColor("link.pressed.foreground", 0xF00000, 0xBA6F25));
        @NotNull Color VISITED = JBColor.namedColor("Link.visitedForeground", JBColor.namedColor("link.visited.foreground", 0x800080, 0x9776A9));
        @NotNull Color SECONDARY = JBColor.namedColor("Link.secondaryForeground", 0x779DBD, 0x5676A0);
    }

}
