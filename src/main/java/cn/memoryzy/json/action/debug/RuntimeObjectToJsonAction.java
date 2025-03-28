package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class RuntimeObjectToJsonAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(RuntimeObjectToJsonAction.class);

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
        Object result;
        try {
            result = JavaDebugUtil.resolveObjectReference(dataContext);
        } catch (StackOverflowError ex) {
            LOG.error(ex);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.runtime.serialize.recursion"), NotificationType.ERROR, e.getProject());
            return;
        }

        if (result != null) {
            ToolWindowUtil.addNewContentWithEditorContentIfNeeded(e.getProject(), JsonUtil.toJsonStr(result), FileTypeHolder.JSON5);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isEnabled(e.getDataContext()));
    }

    public static boolean isEnabled(DataContext dataContext) {
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        Class<?> classClz = JsonAssistantUtil.getClassByName("com.intellij.psi.PsiClass");
        if (languageClz != null && classClz != null) {
            return JavaDebugUtil.isObjectOrListWithChildren(dataContext);
        }

        return false;
    }
}
