package cn.memoryzy.json.action.notification;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/26
 */
public class RecoverAction extends DumbAwareAction {

    public RecoverAction() {
        super(JsonAssistantBundle.messageOnSystem("action.recover.history.text"),
                JsonAssistantBundle.messageOnSystem("action.recover.history.description"),
                null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
