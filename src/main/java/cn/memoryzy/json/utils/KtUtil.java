package cn.memoryzy.json.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
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
public class KtUtil {


    public static KtClass getKtClass(AnActionEvent e) {
        PsiFile psiFile = PlatformUtil.getPsiFile(e);
        Editor editor = PlatformUtil.getEditor(e);

        if (Objects.isNull(psiFile)) {
            return null;
        }

        PsiElement element = PsiUtil.getElementAtOffset(psiFile, editor.getCaretModel().getOffset());
        // 当前元素如果是 ktClass就直接选择，如果不是，则找父元素
        return (element instanceof KtClass) ? (KtClass) element : PsiTreeUtil.getParentOfType(element, KtClass.class);
    }


    /**
     * 获取当前光标所处范围的PsiClass
     */
    public static PsiClass getPsiClass(AnActionEvent e) {
        PsiClass currentPsiClass = null;
        PsiFile psiFile = PlatformUtil.getPsiFile(e);
        if (psiFile instanceof KtFile) {
            KtFile ktFile = (KtFile) psiFile;
            KtClass ktClass = getKtClass(e);
            if (Objects.nonNull(ktClass)) {
                String ktClassFqName = Objects.requireNonNull(ktClass.getFqName()).asString();
                List<PsiClass> psiClassList = getAllClasses(ktFile);
                for (PsiClass psiClass : psiClassList) {
                    String qualifiedName = psiClass.getQualifiedName();
                    if (StrUtil.equals(ktClassFqName, qualifiedName)) {
                        currentPsiClass = psiClass;
                        break;
                    }
                }
            }
        }

        return currentPsiClass;
    }


    public static boolean isKtFile(AnActionEvent e) {
        PsiFile psiFile = PlatformUtil.getPsiFile(e);
        return psiFile instanceof KtFile;
    }


    public static boolean hasKtProperty(AnActionEvent e) {
        // KtClass ktClass = getKtClass(e);
        // if (Objects.isNull(ktClass)) {
        //     return false;
        // }
        //
        // // 如果是数据类，那么 ktClass.getProperties() 获取不到属性，数据类 只能在主构造函数中定义属性
        // return CollUtil.isNotEmpty(ktClass.isData() ? ktClass.getPrimaryConstructorParameters() : ktClass.getProperties());

        PsiClass psiClass = getPsiClass(e);
        if (Objects.isNull(psiClass)) {
            return false;
        }

        // kt 依赖于 Java，所以直接用 Java 工具即可
        PsiField[] fields = JavaUtil.getAllFieldFilterStatic(psiClass);
        return ArrayUtil.isNotEmpty(fields);
    }

    public static List<PsiClass> getAllClasses(KtFile ktFile) {
        PsiClass[] classes = ktFile.getClasses();
        List<PsiClass> psiClassList = new ArrayList<>();
        for (PsiClass psiClass : classes) {
            PsiClass[] innerClasses = psiClass.getInnerClasses();
            psiClassList.add(psiClass);
            if (ArrayUtil.isNotEmpty(innerClasses)) {
                psiClassList.addAll(Arrays.asList(innerClasses));
            }
        }

        return psiClassList;
    }

}
