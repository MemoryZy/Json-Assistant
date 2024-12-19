package cn.memoryzy.json.action.toolwindow;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/20
 */
public class ScrollToTheEndAction extends ScrollToTheEndToolbarAction {

    public ScrollToTheEndAction(@NotNull Editor editor) {
        super(editor);
    }



}
