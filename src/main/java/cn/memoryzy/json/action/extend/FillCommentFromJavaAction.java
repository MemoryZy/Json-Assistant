package cn.memoryzy.json.action.extend;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/5/20
 */
public class FillCommentFromJavaAction extends AnAction implements UpdateInBackground {

    private static final ClassFilter FILTER = el -> ArrayUtil.isNotEmpty(JavaUtil.getNonStaticFields(el));

    public FillCommentFromJavaAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.fillCommentFromJava.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.fillCommentFromJava.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (null == project) return;

        TreeClassChooserFactory factory = TreeClassChooserFactory.getInstance(project);
        TreeClassChooser chooser = factory.createWithInnerClassesScopeChooser(
                JsonAssistantBundle.messageOnSystem("dialog.chooser.class.title"),
                GlobalSearchScope.projectScope(project),
                FILTER,
                null);
        // 展示
        chooser.showDialog();
        // 选择
        PsiClass selected = chooser.getSelected();
        if (null == selected) {
            return;
        }

        // TODO 需要处理递归的类及字段，用 xxx.aaa 表示，解析JSON时也一样表示
        PsiField[] fields = JavaUtil.getNonStaticFields(selected);
        for (PsiField field : fields) {
            // 获取字段注释
            String comment = JavaUtil.resolveFieldComment(field);
            if (StrUtil.isNotBlank(comment)) {



            }



        }




        System.out.println();

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(PlatformUtil.hasJavaEnvironment(getEventProject(e)));
    }



}
