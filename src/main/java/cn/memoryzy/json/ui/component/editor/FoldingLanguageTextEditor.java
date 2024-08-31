package cn.memoryzy.json.ui.component.editor;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/22
 */
public class FoldingLanguageTextEditor extends CustomizedLanguageTextEditor {

    public static final Key<String> PLUGIN_EDITOR_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".Editor");

    public FoldingLanguageTextEditor(Language language, @Nullable Project project, @NotNull String value) {
        super(language, project, value, false);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        EditorSettings settings = editor.getSettings();
        // 设置所示的折叠轮廓
        settings.setFoldingOutlineShown(PlatformUtil.isNewUi());
        // 显示设置插入符行（光标选中行会变黄）
        settings.setCaretRowShown(true);

        int left = 8;
        if (PlatformUtil.isNewUi()) {
            left = 0;
            // 白色背景
            editor.setBackgroundColor(new JBColor(Color.WHITE, new Color(0xFF1E1F22)));
        }

        editor.setBorder(JBUI.Borders.empty(5, left, 0, 0));
        editor.putUserData(PLUGIN_EDITOR_KEY, JsonAssistantPlugin.PLUGIN_ID_NAME);

        return editor;
    }
}