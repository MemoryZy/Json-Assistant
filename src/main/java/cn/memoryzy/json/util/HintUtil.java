package cn.memoryzy.json.util;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ui.accessibility.AccessibleContextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 处理提示信息
 *
 * @author Memory
 * @since 2024/11/26
 */
public class HintUtil {

    /**
     * 展示 info 提示信息<br/>
     * &nbsp;&nbsp; 来源于 {@link com.intellij.codeInsight.hint.HintManager#showInformationHint(Editor, String)}<br/>
     * &nbsp;&nbsp; 更改了触发提示消息消失的事件
     *
     * @param editor 编辑器
     * @param text   提示文本
     */
    public static void showInformationHint(@NotNull Editor editor, @NotNull String text) {
        showInformationHint(editor, text, null);
    }

    public static void showInformationHint(@NotNull Editor editor, @NotNull String text, @Nullable Runnable onHintHidden) {
        short position = HintManager.ABOVE;
        JComponent component = com.intellij.codeInsight.hint.HintUtil.createInformationLabel(
                text, null, null, null);

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
        LightweightHint hint = new LightweightHint(component) {
            @Override
            public boolean vetoesHiding() {
                return true;
            }

            @Override
            public boolean canControlAutoHide() {
                return true;
            }
        };

        Point p = hintManager.getHintPosition(hint, editor, position);
        int flags = HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_CARET_MOVE;

        AccessibleContextUtil.setName(hint.getComponent(), IdeBundle.message("information.hint.accessible.context.name"));
        if (onHintHidden != null) {
            hint.addHintListener((event) -> {
                onHintHidden.run();
            });
        }

        hintManager.showEditorHint(hint, editor, p, flags, 0, false);
    }

}
