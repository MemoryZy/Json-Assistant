package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.Json5Util;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
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
        // 在此判断当前光标是在某个对象 或 对象实例 及 List、数组上，那就将该对象 或 对象实例解析为JSON
        PsiClass psiClass = JavaUtil.getCurrentCursorPositionClass(project, dataContext);
        // 执行操作
        JavaBeanToJsonAction.convertAttributesToJsonAndNotify(project, psiClass, Json5Util::formatJson5WithDoubleQuote, true, LOG);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(JavaBeanToJsonAction.isEnable(getEventProject(event), event.getDataContext()));
    }

}
