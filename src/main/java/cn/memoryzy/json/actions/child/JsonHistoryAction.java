package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonHistoryChooser;
import cn.memoryzy.json.ui.JsonViewerWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction {

    private final JsonViewerWindow window;

    public JsonHistoryAction(JsonViewerWindow window, ToolWindowEx toolWindowEx) {
        super(JsonAssistantBundle.messageOnSystem("action.json.history.text"),
                JsonAssistantBundle.messageOnSystem("action.json.history.description"),
                JsonAssistantIcons.HISTORY);
        this.window = window;

        registerCustomShortcutSet(CustomShortcutSet.fromString("alt H"), toolWindowEx.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        JsonHistoryChooser chooser = new JsonHistoryChooser(project, window);
        ApplicationManager.getApplication().invokeLater(chooser::show);
    }

}
