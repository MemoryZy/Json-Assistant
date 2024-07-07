package cn.memoryzy.json.utils;

import cn.hutool.core.util.ArrayUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
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

    /**
     * 用两种方法获取PsiClass
     *
     * @param event 事件源
     * @return Class
     */
    // public static PsiClass getPsiClass(AnActionEvent event) {
    //     PsiClass psiClass = null;
    //
    //     try {
    //         // 一个类中可能存在几个内部类
    //         PsiClass[] psiClasses = getAllPsiClassByKtFile(event);
    //
    //         if (ArrayUtil.isEmpty(psiClasses)) {
    //             return getCurrentPsiClassByOffset(event);
    //         } else {
    //             // 单独Class
    //             if (psiClasses.length == 1) {
    //                 psiClass = psiClasses[0];
    //             } else {
    //                 // 偏移量获取
    //                 PsiClass curClz = getCurrentPsiClassByOffset(event);
    //                 if (Objects.nonNull(curClz)) {
    //                     psiClass = curClz;
    //                 } else {
    //                     psiClass = psiClasses[0];
    //                 }
    //             }
    //         }
    //     } catch (Throwable ignored) {
    //     }
    //
    //     return psiClass;
    // }


    /**
     * 根据Kt文件获取当前Class文件及所有内部类
     *
     * @param event 事件信息
     * @return Class
     */
    public static PsiClass[] getAllPsiClassByKtFile(AnActionEvent event) {
        List<PsiClass> psiClassList = new ArrayList<>();
        PsiFile psiFile = PlatformUtil.getPsiFile(event);

        if (psiFile instanceof KtFile) {
            KtFile ktFile = (KtFile) psiFile;
            PsiClass[] classes = ktFile.getClasses();
            for (PsiClass psiClass : classes) {
                psiClassList.add(psiClass);
                PsiClass[] innerClasses = psiClass.getInnerClasses();
                if (ArrayUtil.isNotEmpty(innerClasses)) {
                    psiClassList.addAll(Arrays.asList(innerClasses));
                }
            }
        }

        return psiClassList.toArray(new PsiClass[0]);
    }

}
