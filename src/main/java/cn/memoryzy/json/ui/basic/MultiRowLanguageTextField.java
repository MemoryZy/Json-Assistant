package cn.memoryzy.json.ui.basic;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/1/19
 */
public class MultiRowLanguageTextField extends LanguageTextField {

    private final boolean needBorder;

    public MultiRowLanguageTextField(Language language, @Nullable Project project, @NotNull String value, boolean needBorder) {
        super(language, project, value);
        this.needBorder = needBorder;
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        // 水平横幅条
        HorizontalScrollBarEditorCustomization horizontalScrollBarEditorCustomization = HorizontalScrollBarEditorCustomization.ENABLED;
        horizontalScrollBarEditorCustomization.customize(editor);

        // 垂直横幅条
        editor.setVerticalScrollbarVisible(true);

        // 单行模式
        OneLineEditorCustomization oneLineEditorCustomization = OneLineEditorCustomization.DISABLED;
        oneLineEditorCustomization.customize(editor);

        editor.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 开始时横向滚动条定位到初始位置
        LogicalPosition logicalPosition = editor.offsetToLogicalPosition(0);
        editor.getScrollingModel().scrollTo(logicalPosition, ScrollType.RELATIVE);

        if (!needBorder) {
            editor.setBorder(null);
        }

        return editor;
    }

}
