package cn.memoryzy.json.ui.editor;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * 不包含按钮的纯输入框
 *
 * @author Memory
 * @since 2024/12/27
 */
public class SearchTextField2 extends EditorTextField {

    private final Runnable action;

    public SearchTextField2(Project project, FileType fileType, Runnable action) {
        super(project, fileType);
        this.action = action;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && pressed) {
            action.run();
            return true;
        }

        return super.processKeyBinding(ks, e, condition, pressed);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setBorder(JBUI.Borders.empty());
        JComponent component = editor.getComponent();
        component.setBorder(JBUI.Borders.empty(4, 0, 3, 6));
        component.setOpaque(false);
        editor.setBackgroundColor(UIUtil.getTextFieldBackground());
        return editor;
    }
}
