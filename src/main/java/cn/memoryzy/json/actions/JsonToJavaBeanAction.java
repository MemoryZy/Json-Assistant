package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonToJavaBeanWindow;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/07/07
 */
public class JsonToJavaBeanAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(JsonToJavaBeanAction.class);

    public JsonToJavaBeanAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.to.json.javabean.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.to.json.javabean.description"));
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
        new JsonToJavaBeanWindow(project, directory, module).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(isAvailable(dataContext));
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

                // todo 待找到解决方案
                String path = virtualFile.getPath();
                enabled = path.contains("/test/java") || path.contains("/main/java") || path.contains("/test/kotlin") || path.contains("/main/kotlin");
            }
        }

        return enabled;
    }
}
