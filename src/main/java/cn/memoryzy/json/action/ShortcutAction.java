package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ShortcutAction extends DumbAwareAction implements UpdateInBackground {

    public ShortcutAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.shortcut.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.shortcut.description"));
        presentation.setIcon(JsonAssistantIcons.SHORTCUTS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        EditKeymapsDialog dialog = new EditKeymapsDialog(null, ActionHolder.MAIN_ACTION_ID);
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

}
