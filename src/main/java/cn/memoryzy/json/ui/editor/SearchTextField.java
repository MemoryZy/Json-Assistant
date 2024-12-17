package cn.memoryzy.json.ui.editor;

import org.jetbrains.annotations.NonNls;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Memory
 * @since 2024/12/17
 */
public class SearchTextField extends com.intellij.ui.SearchTextField {

    public SearchTextField(@NonNls String historyPropertyName, Runnable action) {
        super(historyPropertyName);
        addKeyboardListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                action.run();
            }
        });
    }


}