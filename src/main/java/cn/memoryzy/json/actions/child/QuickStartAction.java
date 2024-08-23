package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JsonAssistantUtil;
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
    public void actionPerformed(@NotNull AnActionEvent e) {
        JsonAssistantUtil.openOnlineDoc(e.getProject(), true);
    }

}