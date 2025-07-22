package cn.memoryzy.json.ui.editor;

import cn.memoryzy.json.ui.component.ExpandableLanguageEditorSupport;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Expandable;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyListener;
import java.util.Optional;

/**
 * @author Memory
 * @since 2025/7/17
 */
public class ExpandableEditorTextField extends LanguageTextField implements Expandable {

    private final ExpandableLanguageEditorSupport support;

    public ExpandableEditorTextField() {
        this(PlainTextLanguage.INSTANCE);
    }

    public ExpandableEditorTextField(Language language) {
        this("", null, language);
    }

    /**
     * Creates an expandable text field with the default line parser/joiner,
     * that uses a whitespaces to split a string to several lines.
     */
    public ExpandableEditorTextField(@NotNull String text, Project project, Language language) {
        super(language, project, text);
        support = new ExpandableLanguageEditorSupport(this, language, ParametersListUtil.DEFAULT_LINE_PARSER, ParametersListUtil.DEFAULT_LINE_JOINER);
        addNotify();
    }

    public void addKeyListener(KeyListener listener) {
        Optional.ofNullable(getEditor())
                .map(Editor::getContentComponent)
                .ifPresent(component -> component.addKeyListener(listener));
    }

    @Override
    public void expand() {
        JsonAssistantUtil.invokeMethod(support, "expand");
    }

    @Override
    public void collapse() {
        JsonAssistantUtil.invokeMethod(support, "collapse");
    }

    @Override
    public boolean isExpanded() {
        Object isExpanded = JsonAssistantUtil.invokeMethod(support, "isExpanded");
        return Boolean.TRUE.equals(isExpanded);
    }
}
