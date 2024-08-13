package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.PluginConstant;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ShortcutAction extends DumbAwareAction {

    public ShortcutAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.shortcut.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.shortcut.description"));
        presentation.setIcon(JsonAssistantIcons.SHORTCUTS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        EditKeymapsDialog dialog = new EditKeymapsDialog(null, PluginConstant.MAIN_ACTION_ID);
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

}
