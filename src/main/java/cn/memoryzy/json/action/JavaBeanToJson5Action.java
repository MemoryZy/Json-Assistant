package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JavaBeanToJson5Action extends AnAction implements UpdateInBackground {

    public JavaBeanToJson5Action() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.javabean.to.json5.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.javabean.to.json5.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

    }

    @Override
    public void update(@NotNull AnActionEvent event) {

    }

}
