package cn.memoryzy.json.action;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.dialog.JsonToJavaBeanDialog;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/07/07
 */
public class JsonToJavaBeanAction extends DumbAwareAction implements UpdateInBackground {

    private static Class<?> clz;
    private static Method updateMethod;

    static {
        try {
            clz = Class.forName("com.intellij.ide.actions.CreateClassAction");
            updateMethod = ReflectUtil.getMethod(clz, "update", AnActionEvent.class);
        } catch (ClassNotFoundException e) {
            clz = null;
            updateMethod = null;
        }
    }

    public JsonToJavaBeanAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.deserialize.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.deserialize.description"));
        presentation.setIcon(JsonAssistantIcons.GROUP_BY_CLASS);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        // 鼠标右键选择的路径
        IdeView ideView = event.getRequiredData(LangDataKeys.IDE_VIEW);
        // 文件夹(包)
        PsiDirectory directory = ideView.getOrChooseDirectory();
        if (Objects.isNull(directory)) {
            return;
        }

        // 当前 module
        Module module = ModuleUtil.findModuleForPsiElement(directory);
        // 窗口
        new JsonToJavaBeanDialog(project, directory, module).show();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        if (getEventProject(event) == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        try {
            Object action = clz.getDeclaredConstructor().newInstance();
            ReflectUtil.invoke(action, updateMethod, event);
        } catch (Exception ex) {
            final DataContext dataContext = event.getDataContext();
            final Presentation presentation = event.getPresentation();
            if (!presentation.isEnabledAndVisible()) {
                presentation.setEnabledAndVisible(isAvailable(dataContext));
            }
        }
    }

    private boolean isAvailable(DataContext dataContext) {
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor != null && editor.getSelectionModel().hasSelection()) {
            return false;
        }

        boolean enabled = false;
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (project != null && view != null && view.getDirectories().length != 0) {
            PsiDirectory chooseDirectory = view.getOrChooseDirectory();
            if (Objects.nonNull(chooseDirectory)) {
                VirtualFile virtualFile = chooseDirectory.getVirtualFile();
                String path = virtualFile.getPath();
                enabled = path.contains("/test/java") || path.contains("/main/java") || path.contains("/test/kotlin") || path.contains("/main/kotlin");
            }
        }

        return enabled;
    }
}
