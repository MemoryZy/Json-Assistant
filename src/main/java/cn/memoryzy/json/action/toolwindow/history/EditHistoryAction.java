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
public class EditHistoryAction extends DumbAwareAction implements UpdateInBackground {

    public EditHistoryAction() {
        super(JsonAssistantBundle.messageOnSystem("action.edit.history.text"), null, IconUtil.getEditIcon());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
