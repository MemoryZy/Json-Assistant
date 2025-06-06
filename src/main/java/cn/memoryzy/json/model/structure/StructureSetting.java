package cn.memoryzy.json.model.structure;

import cn.memoryzy.json.model.EditorContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

/**
 * 树结构预设的设置
 *
 * @author Memory
 * @since 2025/2/19
 */
public class StructureSetting {

    /**
     * 视图相关配置
     */
    private final ViewConfiguration viewConfig = new ViewConfiguration();

    /**
     * 编辑器上下文
     */
    private final EditorContext editorContext = new EditorContext();


    // region Getter、Setter

    // ------------------- ViewConfiguration 属性代理 -------------------

    public boolean isNeedBorder() {
        return viewConfig.isNeedBorder();
    }

    public StructureSetting setNeedBorder(boolean needBorder) {
        viewConfig.setNeedBorder(needBorder);
        return this;
    }

    public boolean isNeedToolbar() {
        return viewConfig.isNeedToolbar();
    }

    public StructureSetting setNeedToolbar(boolean needToolbar) {
        viewConfig.setNeedToolbar(needToolbar);
        return this;
    }

    public int getExpandLevel() {
        return viewConfig.getExpandLevel();
    }

    public StructureSetting setExpandLevel(int expandLevel) {
        viewConfig.setExpandLevel(expandLevel);
        return this;
    }

    public boolean isNeedRefresh() {
        return viewConfig.isNeedRefresh();
    }

    public StructureSetting setNeedRefresh(boolean needRefresh) {
        viewConfig.setNeedRefresh(needRefresh);
        return this;
    }

    public boolean isLazyLoad() {
        return viewConfig.isLazyLoad();
    }

    public StructureSetting setLazyLoad(boolean lazyLoad) {
        viewConfig.setLazyLoad(lazyLoad);
        return this;
    }

    // ------------------- EditorContext 属性代理 -------------------

    public Editor getEditor() {
        return editorContext.getEditor();
    }

    public StructureSetting setEditor(Editor editor) {
        editorContext.setEditor(editor);
        return this;
    }

    public PsiFile getPsiFile() {
        return editorContext.getPsiFile();
    }

    public StructureSetting setPsiFile(PsiFile psiFile) {
        editorContext.setPsiFile(psiFile);
        return this;
    }

    public VirtualFile getFile() {
        return editorContext.getFile();
    }

    public StructureSetting setFile(VirtualFile file) {
        editorContext.setFile(file);
        return this;
    }

    // ------------------- 组合对象的完整访问方法 -------------------

    public EditorContext getEditorContext() {
        return editorContext;
    }

    public StructureSetting setEditorContext(EditorContext editorContext) {
        this.editorContext
                .setEditor(editorContext.getEditor())
                .setPsiFile(editorContext.getPsiFile())
                .setFile(editorContext.getFile());
        return this;
    }

    // endregion
}
