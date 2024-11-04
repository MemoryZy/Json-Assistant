package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/26
 */
public class QuickStartAction extends DumbAwareAction {

    public QuickStartAction() {
        super(JsonAssistantBundle.messageOnSystem("action.quick.start.text"),
                JsonAssistantBundle.messageOnSystem("action.quick.start.description"),
                AllIcons.General.Web);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        JsonAssistantUtil.openOnlineDoc(event.getProject(), true);
    }

}