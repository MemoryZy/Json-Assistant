package cn.memoryzy.json.ui.color;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义圆形图标
 *
 * @author Memory
 * @since 2024/11/18
 */
public class CircleIcon implements Icon {
    private final int diameter;
    private final Color backgroundColor;

    public CircleIcon(int diameter, Color backgroundColor) {
        this.diameter = diameter;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);
        g2d.setColor(backgroundColor);
        g2d.fillOval(0, 0, diameter, diameter);
        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return diameter;
    }

    @Override
    public int getIconHeight() {
        return diameter;
    }
}