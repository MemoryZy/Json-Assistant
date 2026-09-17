package cn.memoryzy.json.model;

import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * 编辑器相关信息
 *
 * @author Memory
 * @since 2025/6/5
 */
public class EditorContext {

    /**
     * 编辑器
     */
    private WeakReference<Editor> editorReference;

    /**
     * Psi 文件
     */
    private WeakReference<PsiFile> psiFileReference;

    /**
     * 虚拟文件
     */
    private WeakReference<VirtualFile> fileReference;

    /**
     * 临时PSI文件（用于非JSON文件时，且不能用弱引用包裹，不然会用时可能会出现被回收的情况，因为它没有强引用）
     */
    private PsiFile tempPsiFile;

    /**
     * 修改前的文本内容
     */
    private String originalContent;


    /**
     * 获取文本内容（优先顺序：编辑器 > Psi文件 > 虚拟文件）
     */
    public String getContentText() {
        // 1. 从编辑器获取
        Editor editor = getEditor();
        if (editor != null) {
            Document document = editor.getDocument();
            return document.getText();
        }

        // 2. 从PSI文件获取
        PsiFile psiFile = getPsiFile();
        if (psiFile != null && psiFile.isValid()) {
            return psiFile.getText();
        }

        // 3. 从虚拟文件获取
        VirtualFile virtualFile = getFile();
        if (virtualFile != null && virtualFile.isValid()) {
            return PlatformUtil.getContentFromVirtualFile(virtualFile);
        }

        // 4. 如果所有来源都不可用，返回null
        return "";
    }


    // region Getter、Setter
    public @Nullable Editor getEditor() {
        return null != editorReference ? editorReference.get() : null;
    }

    public EditorContext setEditor(Editor editor) {
        this.editorReference = new WeakReference<>(editor);
        return this;
    }

    public @Nullable PsiFile getPsiFile() {
        return null != psiFileReference ? psiFileReference.get() : null;
    }

    public EditorContext setPsiFile(PsiFile psiFile) {
        this.psiFileReference = new WeakReference<>(psiFile);
        return this;
    }

    public @Nullable VirtualFile getFile() {
        return null != fileReference ? fileReference.get() : null;
    }

    public EditorContext setFile(VirtualFile file) {
        this.fileReference = new WeakReference<>(file);
        return this;
    }

    public @Nullable PsiFile getTempPsiFile() {
        return tempPsiFile;
    }

    public EditorContext setTempPsiFile(PsiFile tempPsiFile) {
        this.tempPsiFile = tempPsiFile;
        return this;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    /**
     * 获取有效的PSI文件（优先使用临时文件）
     */
    public PsiFile getEffectivePsiFile() {
        if (tempPsiFile != null) {
            return tempPsiFile;
        }
        return getPsiFile();
    }

    /**
     * 检查是否使用临时文件
     */
    public boolean isUsingTempFile() {
        return tempPsiFile != null;
    }

    // endregion
}
