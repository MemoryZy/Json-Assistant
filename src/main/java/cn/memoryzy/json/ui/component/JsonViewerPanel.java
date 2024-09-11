package cn.memoryzy.json.ui.component;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class JsonViewerPanel extends JPanel {

    private final EditorEx editor;
    private final EditorColorsScheme defaultColorsScheme;

    public JsonViewerPanel(LayoutManager layout, EditorEx editor, EditorColorsScheme defaultColorsScheme) {
        super(layout);
        this.editor = editor;
        this.defaultColorsScheme = defaultColorsScheme;
    }

    @NotNull
    public EditorEx getEditor() {
        return editor;
    }

    public EditorColorsScheme getDefaultColorsScheme() {
        return defaultColorsScheme;
    }
}
