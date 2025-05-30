package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class UpgradeHintAction extends DumbAwareAction implements UpdateInBackground {

    @SuppressWarnings("DialogTitleCapitalization")
    public UpgradeHintAction() {
        super("Updates available", JsonAssistantBundle.messageOnSystem("action.upgrade.description"), JsonAssistantIcons.UPGRADE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Presentation presentation = e.getPresentation();
        if (JsonAssistantPlugin.hasUpdateAvailable()) {
            enabled = true;
            String latestVersion = JsonAssistantPlugin.getLatestVersion();
            presentation.setText(JsonAssistantBundle.messageOnSystem("action.upgrade.text", latestVersion));
        }

        presentation.setEnabledAndVisible(enabled);
    }
}
