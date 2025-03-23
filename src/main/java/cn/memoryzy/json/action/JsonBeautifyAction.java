package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonBeautifyAction extends DumbAwareAction implements UpdateInBackground {

    public JsonBeautifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.DIZZY_STAR);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalJsonConverter.parseAndProcessJson(
                dataContext, PlatformUtil.getEditor(dataContext), true,
                JsonAssistantBundle.messageOnSystem("hint.selection.beautify"),
                JsonAssistantBundle.messageOnSystem("hint.global.beautify"));
    }

}
