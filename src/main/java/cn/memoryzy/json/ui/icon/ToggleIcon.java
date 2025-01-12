package cn.memoryzy.json.ui.icon;

import icons.JsonAssistantIcons;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2025/1/3
 */
public class ToggleIcon implements Icon {

    private boolean selected;
    private final Icon checkedIcon;

    public ToggleIcon(boolean selected) {
        this.selected = selected;
        // this.checkedIcon = LafIconLookup.getIcon("checkmark");
        this.checkedIcon = JsonAssistantIcons.CHECKMARK;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (selected) {
            checkedIcon.paintIcon(c, g, x, y);
        }
    }

    @Override
    public int getIconWidth() {
        return checkedIcon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return checkedIcon.getIconHeight();
    }

    public void prepare(boolean selected) {
        this.selected = selected;
    }
}
