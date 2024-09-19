package cn.memoryzy.json.action.transform;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/19
 */
public class ToYamlAction extends DumbAwareAction {

    public ToYamlAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.to.yaml.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.to.yaml.description"));
        // presentation.setIcon();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        throw new RuntimeException();
    }
}
