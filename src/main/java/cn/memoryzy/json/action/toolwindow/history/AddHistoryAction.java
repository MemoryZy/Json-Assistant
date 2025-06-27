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
public class AddHistoryAction extends DumbAwareAction implements UpdateInBackground {

    public AddHistoryAction() {
        super(JsonAssistantBundle.messageOnSystem("action.add.history.text"), null, IconUtil.getAddIcon());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
