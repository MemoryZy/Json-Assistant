package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/7/7
 */
public class OpenHistoryInEditorAction extends DumbAwareAction implements UpdateInBackground {

    public OpenHistoryInEditorAction() {
        super(JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.text"), JsonAssistantBundle.messageOnSystem("action.openHistoryInEditor.description"), null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
