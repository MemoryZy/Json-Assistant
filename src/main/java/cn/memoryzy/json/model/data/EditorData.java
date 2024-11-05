package cn.memoryzy.json.model.data;

import com.intellij.openapi.editor.Caret;

/**
 * Editor 相关信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class EditorData {

    /**
     * 当前编辑器中 '主要' 的光标（插入符）
     */
    private Caret primaryCaret;

    /**
     * 文档内文本信息
     */
    private DocTextData docTextData;

    /**
     * 编辑器内关于文本的相关信息
     */
    private SelectionData selectionData;



    // ----------------------- GETTER/SETTER -----------------------

    public Caret getPrimaryCaret() {
        return primaryCaret;
    }

    public EditorData setPrimaryCaret(Caret primaryCaret) {
        this.primaryCaret = primaryCaret;
        return this;
    }

    public DocTextData getDocTextData() {
        return docTextData;
    }

    public EditorData setDocTextData(DocTextData docTextData) {
        this.docTextData = docTextData;
        return this;
    }

    public SelectionData getSelectionData() {
        return selectionData;
    }

    public EditorData setSelectionData(SelectionData selectionData) {
        this.selectionData = selectionData;
        return this;
    }
}
