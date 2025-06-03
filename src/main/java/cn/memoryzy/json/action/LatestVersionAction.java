package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/3
 */
public class LatestVersionAction extends DumbAwareAction implements UpdateInBackground {

    public LatestVersionAction() {
        super(JsonAssistantBundle.messageOnSystem("action.latest.version.text"), JsonAssistantBundle.messageOnSystem("action.latest.version.description"), null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO 待实现
    }
}
