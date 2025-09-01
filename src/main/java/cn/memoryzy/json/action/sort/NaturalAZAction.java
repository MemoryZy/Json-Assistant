package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class NaturalAZAction extends DumbAwareAction implements UpdateInBackground {

    public NaturalAZAction() {
        super(JsonAssistantBundle.message("action.naturalAZ.text"), JsonAssistantBundle.messageOnSystem("action.naturalAZ.description"), null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
