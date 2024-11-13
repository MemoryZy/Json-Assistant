package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.KotlinUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KotlinPropertyToJsonAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(KotlinPropertyToJsonAction.class);

    public KotlinPropertyToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kt.class.property.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kt.class.property.to.json.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        // 获取当前类
        PsiClass currentPsiClass = KotlinUtil.getPsiClass(event.getDataContext());
        // 执行操作
        JavaBeanToJsonAction.convertAttributesToJsonAndNotify(project, currentPsiClass, JsonUtil::formatJson, LOG);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置可见性
        event.getPresentation().setEnabledAndVisible(isEnable(getEventProject(event), event.getDataContext()));
    }

    public static boolean isEnable(Project project, DataContext dataContext) {
        return Objects.nonNull(project) && KotlinUtil.isKtFile(dataContext) && KotlinUtil.hasKtProperty(dataContext);
    }

}
