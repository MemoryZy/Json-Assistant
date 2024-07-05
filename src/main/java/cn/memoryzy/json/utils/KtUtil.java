package cn.memoryzy.json.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KtUtil {

    /**
     * 根据编辑器的偏移量获取当前所在的kt文件（因为利用了编辑器，光标必须在类中，也就是类的上下作用域内）
     *
     * @param event 事件信息
     * @return class
     */
    public static KtFile getCurrentPsiClassByOffset(AnActionEvent event) {
        PsiFile psiFile = PlatformUtil.getPsiFile(event);
        Editor editor = PlatformUtil.getEditor(event);

        if (Objects.nonNull(psiFile) && (psiFile.getFileType() instanceof KotlinFileType)) {
            return PsiTreeUtil.getParentOfType(PlatformUtil.getPsiElementByOffset(editor, psiFile), KtFile.class);
        }

        return null;
    }

}
