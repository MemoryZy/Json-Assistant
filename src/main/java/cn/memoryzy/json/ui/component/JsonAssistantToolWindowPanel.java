package cn.memoryzy.json.ui.component;

import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonAssistantToolWindowPanel extends JPanel {

    private final EditorEx editor;

    public JsonAssistantToolWindowPanel(LayoutManager layout, EditorEx editor) {
        super(layout);
        this.editor = editor;
    }

    @NotNull
    public EditorEx getEditor() {
        return editor;
    }

}
