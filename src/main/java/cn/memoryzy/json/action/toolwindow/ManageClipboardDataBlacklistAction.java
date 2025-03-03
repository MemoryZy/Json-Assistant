package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.dialog.JsonBlacklistDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/3/3
 */
public class ManageClipboardDataBlacklistAction extends DumbAwareAction {

    public ManageClipboardDataBlacklistAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.manage.clipboard.data.blacklist.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.manage.clipboard.data.blacklist.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new JsonBlacklistDialog(getEventProject(e)).show();
    }
}
