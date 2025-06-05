package cn.memoryzy.json.model;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

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
    private Editor editor;

    /**
     * Psi 文件
     */
    private PsiFile psiFile;

    /**
     * 虚拟文件
     */
    private VirtualFile file;


    // region Getter、Setter
    public Editor getEditor() {
        return editor;
    }

    public EditorContext setEditor(Editor editor) {
        this.editor = editor;
        return this;
    }

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public EditorContext setPsiFile(PsiFile psiFile) {
        this.psiFile = psiFile;
        return this;
    }

    public VirtualFile getFile() {
        return file;
    }

    public EditorContext setFile(VirtualFile file) {
        this.file = file;
        return this;
    }
    // endregion
}
