package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/6/21
 */
public class JsonMinifyAction extends DumbAwareAction {
    private static final Logger LOG = Logger.getInstance(JsonMinifyAction.class);

    public JsonMinifyAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.minify.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalJsonConverter.parseAndProcessJson(
                dataContext, PlatformUtil.getEditor(dataContext), false,
                JsonAssistantBundle.messageOnSystem("hint.selection.json.minify.text"),
                JsonAssistantBundle.messageOnSystem("hint.global.json.minify.text"));
    }

}
