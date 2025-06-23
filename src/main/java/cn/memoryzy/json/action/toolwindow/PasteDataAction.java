package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/23
 */
public class PasteDataAction extends DumbAwareAction implements UpdateInBackground {

    public PasteDataAction() {
        super(JsonAssistantBundle.messageOnSystem("action.paste.data.text"), JsonAssistantBundle.messageOnSystem("action.paste.data.description"), AllIcons.Actions.MenuPaste);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
