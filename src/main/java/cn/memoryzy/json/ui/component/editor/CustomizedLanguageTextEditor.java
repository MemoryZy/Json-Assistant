package cn.memoryzy.json.ui.component.editor;

import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.impl.DelegateColorScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HorizontalScrollBarEditorCustomization;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.OneLineEditorCustomization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/1/19
 */
public class CustomizedLanguageTextEditor extends LanguageTextField {

    private final boolean needBorder;

    public CustomizedLanguageTextEditor(Language language, @Nullable Project project, @NotNull String value, boolean needBorder) {
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

        EditorSettings editorSettings = editor.getSettings();
        // 设置空白显示（由.代替空白）
        // editorSettings.setWhitespacesShown(true);
        // 设置显示的缩进导轨
        editorSettings.setIndentGuidesShown(true);
        // // 显示设置插入符行（光标选中行会变黄）
        // editorSettings.setCaretRowShown(true);

        // // 设置显示的线标记区域
        // editorSettings.setLineMarkerAreaShown(false);
        // // 设置行号显示
        // editorSettings.setLineNumbersShown(false);
        // // 设置所示的折叠轮廓
        // editorSettings.setFoldingOutlineShown(false);
        // // 在底部设置附加页面
        // editorSettings.setAdditionalPageAtBottom(false);
        // // 设置附加列数
        // editorSettings.setAdditionalColumnsCount(0);
        // // 设置附加行数
        // editorSettings.setAdditionalLinesCount(0);
        // // 设置显示的右边距
        // editorSettings.setRightMarginShown(false);
        // // 显示特殊字符的集合
        // editorSettings.setShowingSpecialChars(false);

        EditorGutterComponentEx gutterComponentEx = editor.getGutterComponentEx();
        // 设置绘画背景
        gutterComponentEx.setPaintBackground(false);

        DelegateColorScheme scheme = ConsoleViewUtil.updateConsoleColorScheme(editor.getColorsScheme());
        if (UISettings.getInstance().getPresentationMode()) {
            scheme.setEditorFontSize(UISettings.getInstance().getPresentationModeFontSize());
        }
        editor.setColorsScheme(scheme);

        return editor;
    }

}
