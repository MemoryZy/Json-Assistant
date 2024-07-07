package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonToJavaBeanWindow;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        // 当前 module
        Module module = event.getData(PlatformDataKeys.MODULE);
        if (Objects.isNull(project) || Objects.isNull(module)) {
            return;
        }

        // 鼠标右键选择的路径
        IdeView ideView = event.getRequiredData(LangDataKeys.IDE_VIEW);
        // 文件夹(包)
        PsiDirectory directory = ideView.getOrChooseDirectory();
        // 窗口
        new JsonToJavaBeanWindow(project, directory, module).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enable = false;
        // 当前工程
        Project project = e.getProject();
        if (Objects.nonNull(project)) {
            // 鼠标右键选择的路径
            IdeView ideView = null;
            try {
                ideView = e.getRequiredData(LangDataKeys.IDE_VIEW);
            } catch (Throwable ex) {
                LOG.debug("## 鼠标右键: " + ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
            }

            if (Objects.nonNull(ideView) && ideView.getDirectories().length > 0) {
                // 文件夹(包)
                PsiDirectory directory = ideView.getOrChooseDirectory();
                if (Objects.nonNull(directory)) {
                    VirtualFile virtualFile = directory.getVirtualFile();
                    String path = virtualFile.getPath();
                    enable = path.contains("/test/java") || path.contains("/main/java");
                }
            }
        }

        // 设置可见性
        e.getPresentation().setVisible(enable);
    }

}
