package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class JsonBeautifyToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final JsonViewerWindow window;

    public JsonBeautifyToolWindowAction(JsonViewerWindow window, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.DIZZY_STAR_ORI);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt B"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {

    }
}
