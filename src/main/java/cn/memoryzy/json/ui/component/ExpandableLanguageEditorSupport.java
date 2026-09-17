package cn.memoryzy.json.ui.component;

import com.intellij.lang.Language;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ExpandableEditorSupport;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/7/17
 */
public class ExpandableLanguageEditorSupport extends ExpandableEditorSupport {

    private final Language popupLanguage;

    public ExpandableLanguageEditorSupport(@NotNull LanguageTextField field, Language popupLanguage) {
        super(field);
        this.popupLanguage = popupLanguage;
    }

    public ExpandableLanguageEditorSupport(@NotNull LanguageTextField field, Language popupLanguage, @NotNull Function<? super String, ? extends List<String>> parser, @NotNull Function<? super List<String>, String> joiner) {
        super(field, parser, joiner);
        this.popupLanguage = popupLanguage;
    }

    @Override
    protected @NotNull EditorTextField createPopupEditor(@NotNull EditorTextField field, @NotNull String text) {
        return new LanguageTextField(popupLanguage, field.getProject(), text, false);
    }
}
