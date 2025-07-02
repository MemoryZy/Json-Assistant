package cn.memoryzy.json.ui.editor;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class ModernTextFieldWithAutoCompletion extends TextFieldWithAutoCompletion<String> {
    
    public ModernTextFieldWithAutoCompletion(@Nullable Project project, Collection<String> variants) {
        super(project, new StringsCompletionProvider(variants, null), false, null);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setBorder(JBUI.Borders.empty());
        JComponent component = editor.getComponent();
        component.setBorder(JBUI.Borders.empty(5, 0, 3, 6));
        component.setOpaque(false);
        editor.setBackgroundColor(UIUtil.getTextFieldBackground());
        return editor;
    }

}
