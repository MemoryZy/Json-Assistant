package cn.memoryzy.json.ui.basic.editor;

import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/22
 */
public class FoldingLanguageTextEditor extends CustomizedLanguageTextEditor {

    public FoldingLanguageTextEditor(Language language, @Nullable Project project, @NotNull String value, boolean needBorder) {
        super(language, project, value, needBorder);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        EditorSettings settings = editor.getSettings();
        // 设置所示的折叠轮廓
        settings.setFoldingOutlineShown(PlatformUtil.isNewUi());
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(true);

        @SuppressWarnings("UseJBColor") Color dark = PlatformUtil.isNewUi() ? new Color(0xFF1E1F22) : Gray._43;
        JBColor color = new JBColor(Color.WHITE, dark);
        editor.setBackgroundColor(color);

        return editor;
    }
}
