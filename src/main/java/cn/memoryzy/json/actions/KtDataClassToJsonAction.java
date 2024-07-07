package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClassForFacade;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

            List<PsiClass> classList = new ArrayList<>();

            for (PsiClass psiClass : classes) {
                PsiClass[] innerClasses = psiClass.getInnerClasses();

                PsiField[] psiFields = JavaUtil.getAllFieldFilterStatic(psiClass);

                for (PsiClass innerClass : innerClasses) {
                    PsiField[] psiFields2 = JavaUtil.getAllFieldFilterStatic(innerClass);
                    System.out.println();
                }

                classList.add(psiClass);
                classList.addAll(Arrays.asList(innerClasses));

                System.out.println();
            }

            System.out.println();
        }








        System.out.println();
    }

}
