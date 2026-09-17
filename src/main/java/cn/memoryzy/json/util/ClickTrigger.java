package cn.memoryzy.json.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ClickTrigger {
    private int clickCount = 0;
    private Timer resetTimer;

    public void init(Component targetComponent, Consumer<?> consumer) {
        targetComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(consumer);
            }
        });

        // 创建3秒重置计时器
        resetTimer = new Timer(3000, ev -> {
            clickCount = 0;
            resetTimer.stop();
        });
        resetTimer.setRepeats(false);
    }

    private void handleClick(Consumer<?> consumer) {
        clickCount++;
        if (!resetTimer.isRunning()) {
            resetTimer.start();
        }

        if (clickCount >= 5) {
            consumer.accept(null);
            resetTimer.stop();
            clickCount = 0;
        }
    }
}