package cn.memoryzy.json.ui.component.editor;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/8/22
 */
public class ViewerModeLanguageTextEditor extends CustomizedLanguageTextEditor {

    public ViewerModeLanguageTextEditor(Language language, @Nullable Project project, @NotNull String value, boolean needBorder) {
        super(language, project, value, needBorder);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        EditorSettings settings = editor.getSettings();
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(true);
        // 编辑器设为-只读
        editor.setViewer(true);
        return editor;
    }
}
