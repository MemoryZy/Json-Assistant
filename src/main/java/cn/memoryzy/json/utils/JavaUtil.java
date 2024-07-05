package cn.memoryzy.json.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.FileASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/3
 */
public class JavaUtil {

    /**
     * 判断是否为引用类型（自己项目中的类）（不包括String、BigDecimal之类的）
     *
     * @param psiType 类型
     * @return true，引用类型；false，不为引用类型
     */
    public static boolean isReferenceType(PsiType psiType) {
        // 不为引用类型
        if (!(psiType instanceof PsiClassReferenceType)) {
            return false;
        }

        // 全限定名（基本类型就只有基本类型名 long、int）
        String canonicalText = psiType.getCanonicalText();
        // 判断是否为java包其他类
        return !StrUtil.startWith(canonicalText, "java.");
    }

    /**
     * 用两种方法获取PsiClass
     *
     * @param event 事件源
     * @return Class
     */
    public static PsiClass getPsiClass(AnActionEvent event) {
        PsiClass psiClass = null;
        try {
            psiClass = getCurrentPsiClassByFile(event);
            if (Objects.isNull(psiClass)) {
                psiClass = getCurrentPsiClassByOffset(event);
            }
        } catch (Throwable e) {

        }

        return psiClass;
    }


    /**
     * 根据Java文件获取当前Class文件
     *
     * @param event 事件信息
     * @return Class
     */
    public static PsiClass getCurrentPsiClassByFile(AnActionEvent event) {
        PsiClass psiClass = null;
        PsiFile psiFile = PlatformUtil.getPsiFile(event);
        if (Objects.nonNull(psiFile)) {
            FileASTNode node = psiFile.getNode();
            PsiElement psi = node.getPsi();
            if (psi instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psi;
                PsiClass[] classes = psiJavaFile.getClasses();
                psiClass = classes[0];
            }
        }

        return psiClass;
    }

    /**
     * 根据编辑器的偏移量获取当前所在的Class文件（因为利用了编辑器，光标必须在类中，也就是类的上下作用域内）
     *
     * @param event 事件信息
     * @return class
     */
    public static PsiClass getCurrentPsiClassByOffset(AnActionEvent event) {
        PsiFile psiFile = PlatformUtil.getPsiFile(event);
        Editor editor = PlatformUtil.getEditor(event);

        if (Objects.nonNull(psiFile) && (psiFile.getFileType() instanceof JavaFileType)) {
            return PsiTreeUtil.getParentOfType(PlatformUtil.getPsiElementByOffset(editor, psiFile), PsiClass.class);
        }

        return null;
    }

    /**
     * 是否处于Java文件中
     *
     * @param event 事件源
     * @return true -> 处于；false -> 不处于
     */
    public static boolean isJavaFile(AnActionEvent event) {
        // 获取当前选中的 PsiClass
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        return psiFile instanceof PsiJavaFile;
    }

    /**
     * 当前 Java 类中是否存在属性
     *
     * @param event 事件源
     * @return true -> 存在；false -> 不存在
     */
    public static boolean hasJavaProperty(AnActionEvent event) {
        boolean enabled = false;
        PsiClass psiClass = getPsiClass(event);
        if (Objects.nonNull(psiClass)) {
            PsiField[] fields = getAllFieldFilterStatic(psiClass);
            enabled = ArrayUtil.isNotEmpty(fields);
        }

        return enabled;
    }


    /**
     * 通过引用类型获取Class
     *
     * @param psiType 引用类型，必须是类引用类型{@link PsiClassReferenceType}
     * @return Class
     */
    public static PsiClass getPsiClassByReferenceType(PsiType psiType) {
        if (psiType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiType;
            return psiClassReferenceType.resolve();
        }

        return null;
    }

    /**
     * 获取该类的所有字段
     *
     * @param psiClass class
     * @return 所有字段
     */
    public static PsiField[] getAllFieldFilterStatic(PsiClass psiClass) {
        return (Objects.isNull(psiClass))
                ? new PsiField[0]
                : Arrays.stream(psiClass.getAllFields()).filter(el -> !el.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiField[]::new);
    }

    /**
     * 获取注解中的指定属性（去除"之后）
     *
     * @param psiAnnotation 注解
     * @param attributeName 属性名
     * @return 属性值
     */
    public static String getMemberValue(PsiAnnotation psiAnnotation, String attributeName) {
        String value = "";
        if (Objects.isNull(psiAnnotation)) {
            return value;
        }

        // 获取注解的属性
        PsiAnnotationMemberValue memberValue = psiAnnotation.findAttributeValue(attributeName);
        if (Objects.isNull(memberValue)) {
            return value;
        }

        // value属性值
        value = memberValue.getText();
        if (StringUtils.isNotBlank(value)) {
            value = value.replace("\"", "");
        }

        return value;
    }
}
