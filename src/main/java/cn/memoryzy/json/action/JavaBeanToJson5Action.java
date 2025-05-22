package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.Json5Util;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JavaBeanToJson5Action extends AnAction implements UpdateInBackground {
    private static final Logger LOG = Logger.getInstance(JavaBeanToJson5Action.class);

    public JavaBeanToJson5Action() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.json5.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.json5.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        JavaBeanToJsonAction.convertAttributesToJsonAndNotify(project, dataContext, false, Json5Util::formatJson5WithDoubleQuote, true, LOG);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        presentation.setEnabledAndVisible(
                JavaBeanToJsonAction.updateActionAndCheckEnablement(getEventProject(event), event.getDataContext(), presentation, true));
    }

}
