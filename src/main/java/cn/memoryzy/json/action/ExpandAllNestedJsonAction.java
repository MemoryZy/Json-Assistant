package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/2/25
 */
public class ExpandAllNestedJsonAction extends DumbAwareAction {

    public ExpandAllNestedJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.expand.nested.Json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.expand.nested.Json.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
