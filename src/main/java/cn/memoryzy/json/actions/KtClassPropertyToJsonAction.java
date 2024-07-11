package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.KtUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KtClassPropertyToJsonAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(KtClassPropertyToJsonAction.class);

    public KtClassPropertyToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kt.class.property.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kt.class.property.to.json.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass currentPsiClass = KtUtil.getPsiClass(e);
        PsiField[] fields = JavaUtil.getAllFieldFilterStatic(currentPsiClass);

        PsiMethod[] allMethods = currentPsiClass.getAllMethods();

        System.out.println();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 设置可见性
        e.getPresentation().setEnabledAndVisible(KtUtil.isKtFile(e) && KtUtil.hasKtProperty(e));
    }
}
