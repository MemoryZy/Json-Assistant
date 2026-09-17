package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonConversionTarget;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.TypeScriptUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

/**
 * 将 JavaBean 转换为 TypeScript 类型声明
 *
 * @author Memory
 * @since 2026/09/02
 */
public class JavaBeanToTypeScriptAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(JavaBeanToTypeScriptAction.class);

    public JavaBeanToTypeScriptAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.ts.serialize.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.ts.serialize.description"));
        presentation.setIcon(JsonAssistantIcons.FileTypes.TYPESCRIPT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        if (project == null) {
            return;
        }

        // 获取光标所在位置的类（支持引用类、局部变量、类字段、方法参数等）
        ImmutablePair<JsonConversionTarget, PsiClass> pair = JavaUtil.getCurrentCursorPositionClass2(project, dataContext);
        PsiClass psiClass = pair.getRight();
        if (psiClass == null) {
            return;
        }

        try {
            String ts = TypeScriptUtil.javaBeanToTypeScript(project, psiClass, true);
            if (StrUtil.isBlank(ts)) {
                return;
            }

            PlatformUtil.setClipboard(ts);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notification.ts.copy"), NotificationType.INFORMATION, project);
        } catch (Exception e) {
            LOG.error(e);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.ts.convert"), NotificationType.ERROR, project);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        presentation.setEnabledAndVisible(isEnable(getEventProject(event), event.getDataContext()));
    }

    private static boolean isEnable(Project project, DataContext dataContext) {
        if (project == null) {
            return false;
        }

        if (!JavaUtil.isJavaFile(dataContext)) {
            return false;
        }

        ImmutablePair<JsonConversionTarget, PsiClass> pair = JavaUtil.getCurrentCursorPositionClass2(project, dataContext);
        PsiClass psiClass = pair.getRight();
        if (psiClass == null) {
            return false;
        }

        return JavaUtil.hasJavaProperty(psiClass);
    }
}
