package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KtDataClassToJsonAction extends AnAction {

    public KtDataClassToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kt.dataclass.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kt.dataclass.to.json.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = PlatformUtil.getPsiFile(e);

        if (psiFile instanceof KtFile) {
            KtFile ktFile = (KtFile) psiFile;
            PsiClass[] classes = ktFile.getClasses();


            PsiClass psiClass = classes[0];

            PsiField[] fields = JavaUtil.getAllFieldFilterStatic(psiClass);

        }








        System.out.println();
    }

}
