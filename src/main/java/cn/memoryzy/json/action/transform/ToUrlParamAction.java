package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class ToUrlParamAction extends DumbAwareAction {

    public ToUrlParamAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.url.param.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.url.param.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.URL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // JsonMap中跳过Map、List、null、长文本String

    }

}
