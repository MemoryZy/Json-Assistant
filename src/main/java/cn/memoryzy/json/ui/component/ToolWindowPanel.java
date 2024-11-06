package cn.memoryzy.json.ui.component;

import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class ToolWindowPanel extends JPanel {

    private final EditorEx editor;

    public ToolWindowPanel(LayoutManager layout, EditorEx editor) {
        super(layout);
        this.editor = editor;
    }

    @NotNull
    public EditorEx getEditor() {
        return editor;
    }

}
