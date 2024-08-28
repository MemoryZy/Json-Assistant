package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.actions.JsonMinifyAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonAssistantUtil;
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

    private final JsonViewerWindow window;

    public JsonMinifyToolWindowAction(JsonViewerWindow window, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.minify.description"));
        presentation.setIcon(JsonAssistantIcons.MINIFY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LanguageTextField jsonTextField = window.getJsonTextField();
        JsonMinifyAction.handleJsonMinify(e, Objects.requireNonNull(jsonTextField.getEditor()));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(null != e.getProject()
                && null != window.getJsonTextField().getEditor()
                && JsonAssistantUtil.isJsonOrExtract(window.getJsonContent()));
    }
}
