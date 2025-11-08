package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/11/9
 */
public class OpenToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    public OpenToolWindowAction() {
        setEnabledInModalContext(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantWindow(getEventProject(e));
        if (toolWindow != null) {
            toolWindow.show();
        }
    }
}
