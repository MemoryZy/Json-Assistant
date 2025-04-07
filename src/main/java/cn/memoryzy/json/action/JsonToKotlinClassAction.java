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
 * @since 2025/4/7
 */
public class JsonToKotlinClassAction extends AnAction implements UpdateInBackground {

    public JsonToKotlinClassAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kotlin.deserialize.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kotlin.deserialize.description"));
        // TODO 图标更换
        presentation.setIcon(JsonAssistantIcons.GROUP_BY_CLASS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
