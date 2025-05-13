package cn.memoryzy.json.ui.editor;

import org.jetbrains.annotations.NonNls;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 另一个实现可以看 {@link com.intellij.jsonpath.ui.JsonPathEvaluateView} 类
 *
 * @author Memory
 * @since 2024/12/17
 */
public class SearchTextField extends com.intellij.ui.SearchTextField {

    public SearchTextField(@NonNls String historyPropertyName, int historySize, Runnable action) {
        super(historyPropertyName);
        setHistorySize(historySize);
        addKeyboardListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 检查是否按下了Enter键
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    action.run();
                }
            }
        });
    }

    @Override
    protected void onFieldCleared() {

    }


}