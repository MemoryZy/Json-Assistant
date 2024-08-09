package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction {

    public JsonHistoryAction() {
        super(JsonAssistantBundle.message("action.json.history.text"),
                JsonAssistantBundle.messageOnSystem("action.json.history.description"),
                null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {


    }

}
