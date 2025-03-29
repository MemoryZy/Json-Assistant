package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class RuntimeObjectToJsonAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(RuntimeObjectToJsonAction.class);
    public static final Key<Boolean> RESOLVE_COMMENT_KEY = Key.create(JsonAssistantPlugin.PLUGIN_ID_NAME + ".RESOLVE_COMMENT");

    public RuntimeObjectToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.runtime.object.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.runtime.object.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        handleObjectReferenceResolution(e.getProject(), dataContext, JsonUtil::toJsonStr, false);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isEnabled(e.getDataContext()));
    }

    public static boolean isEnabled(DataContext dataContext) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        Class<?> classClz = JsonAssistantUtil.getClassByName("com.intellij.psi.PsiClass");
        if (project != null && languageClz != null && classClz != null) {
            return JavaDebugUtil.isObjectOrListWithChildren(dataContext);
        }

        return false;
    }

    public static void handleObjectReferenceResolution(Project project, @NotNull DataContext dataContext, Function<Object, String> jsonConverter, boolean resolveComment) {
        Object result;
        try {
            // 保存【是否解析注释】
            if (resolveComment) project.putUserData(RESOLVE_COMMENT_KEY, true);
            result = JavaDebugUtil.resolveObjectReference(project, dataContext);
        } catch (StackOverflowError ex) {
            LOG.error(ex);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.runtime.serialize.recursion"), NotificationType.ERROR, project);
            return;
        } finally {
            // 置空
            project.putUserData(RESOLVE_COMMENT_KEY, null);
        }

        if (result != null) {
            ToolWindowUtil.addNewContentWithEditorContentIfNeeded(project, jsonConverter.apply(result), FileTypeHolder.JSON5);
        }
    }

}
