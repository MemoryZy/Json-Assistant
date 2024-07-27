package cn.memoryzy.json.actions;

import cn.memoryzy.json.utils.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/26
 */
public class Recover extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        // propertiesComponent.unsetValue(JsonAssistantPlugin.PLUGIN_VERSION);

        Notifications.showWelcomeNotification(e.getProject());
    }


}
