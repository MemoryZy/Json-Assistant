package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClassForFacade;
import org.jetbrains.kotlin.psi.KtClass;
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
        Editor editor = PlatformUtil.getEditor(e);

        if (Objects.isNull(psiFile)) {
            return;
        }

        PsiElement element = PsiUtil.getElementAtOffset(psiFile, editor.getCaretModel().getOffset());
        // 当前元素如果是 ktClass就直接选择，如果不是，则找父元素
        KtClass ktClass = (element instanceof KtClass) ? (KtClass) element : PsiTreeUtil.getParentOfType(element, KtClass.class);




        System.out.println();
    }

}
