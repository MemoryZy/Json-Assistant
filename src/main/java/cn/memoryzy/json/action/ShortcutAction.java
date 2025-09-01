package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionIdHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
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
        new EditKeymapsDialog(null, ActionIdHolder.MAIN_ACTION_ID).show();
    }

}
