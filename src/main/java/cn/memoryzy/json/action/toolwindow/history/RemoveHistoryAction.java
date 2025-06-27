package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/27
 */
public class RemoveHistoryAction extends DumbAwareAction implements UpdateInBackground  {

    public RemoveHistoryAction() {
        super(JsonAssistantBundle.messageOnSystem("action.remove.history.text"), null, IconUtil.getRemoveIcon());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
