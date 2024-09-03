package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.action.JsonMinifyAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.LanguageTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class JsonMinifyToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final LanguageTextField languageTextField;

    public JsonMinifyToolWindowAction(LanguageTextField languageTextField, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.languageTextField = languageTextField;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.minify.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.MINIFY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JsonMinifyAction.handleJsonMinify(e, Objects.requireNonNull(languageTextField.getEditor()));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(null != e.getProject()
                && null != languageTextField.getEditor()
                && JsonAssistantUtil.isJsonOrExtract(languageTextField.getText()));
    }
}
