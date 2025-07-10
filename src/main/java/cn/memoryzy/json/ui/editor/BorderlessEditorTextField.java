package cn.memoryzy.json.ui.editor;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2025/6/30
 */
public class BorderlessEditorTextField extends EditorTextField {

    public BorderlessEditorTextField(Project project, FileType fileType) {
        super(project, fileType);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setBorder(JBUI.Borders.empty());
        JComponent component = editor.getComponent();
        component.setOpaque(false);
        editor.setBackgroundColor(UIUtil.getTextFieldBackground());
        return editor;
    }

}
