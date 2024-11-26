package cn.memoryzy.json.action.notification;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/26
 */
public class IgnoreAction extends DumbAwareAction {

    public IgnoreAction() {
        super(JsonAssistantBundle.messageOnSystem("action.ignore.text"),
                JsonAssistantBundle.messageOnSystem("action.ignore.description"),
                null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
