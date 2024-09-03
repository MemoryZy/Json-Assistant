package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.action.JsonBeautifyAction;
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
public class JsonBeautifyToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final LanguageTextField languageTextField;

    public JsonBeautifyToolWindowAction(LanguageTextField languageTextField, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.languageTextField = languageTextField;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.PENCIL_STAR);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt B"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JsonBeautifyAction.handleJsonBeautify(e, Objects.requireNonNull(languageTextField.getEditor()));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(null != e.getProject()
                && null != languageTextField.getEditor()
                && JsonAssistantUtil.isJsonOrExtract(languageTextField.getText()));
    }
}
