package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.SupportDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class DonateAction extends DumbAwareAction {

    public DonateAction(String text) {
        super(text, JsonAssistantBundle.messageOnSystem("action.donate.description"), JsonAssistantIcons.DONATE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SupportDialog dialog = new SupportDialog();
        ApplicationManager.getApplication().invokeLater(dialog::show);
    }

}
