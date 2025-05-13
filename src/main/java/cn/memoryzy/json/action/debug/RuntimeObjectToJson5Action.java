package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypes;
import cn.memoryzy.json.util.JavaDebugUtil;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/3/28
 */
public class RuntimeObjectToJson5Action extends AnAction implements UpdateInBackground {

    public RuntimeObjectToJson5Action() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.json5.runtime.object.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.json5.runtime.object.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        RuntimeObjectToJsonAction.handleObjectReferenceResolution(e.getProject(), dataContext, Json5Util::toJson5StrWithDoubleQuote, true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isEnabled(e.getProject(), e.getDataContext()));
    }

    private static boolean isEnabled(@Nullable Project project, DataContext dataContext) {
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        Class<?> classClz = JsonAssistantUtil.getClassByName("com.intellij.psi.PsiClass");
        if (project != null && languageClz != null && classClz != null) {
            return JavaDebugUtil.isJavaBeanOrContainsJavaBeans(project, dataContext);
        }

        return false;
    }

}
