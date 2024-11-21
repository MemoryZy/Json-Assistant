package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.dialog.SupportDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
    public void actionPerformed(@NotNull AnActionEvent event) {
        new SupportDialog().show();
    }

}
