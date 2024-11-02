package cn.memoryzy.json.model.formats;

import com.intellij.openapi.editor.Caret;

/**
 * Editor 相关信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class EditorInfo {

    /**
     * 当前编辑器中 '主要' 的光标（插入符）
     */
    private Caret primaryCaret;

    /**
     * 文档内文本信息
     */
    private DocumentTextInfo documentTextInfo;

    /**
     * 编辑器内关于文本的相关信息
     */
    private SelectionInfo selectionInfo;



    // ----------------------- GETTER/SETTER -----------------------

    public Caret getPrimaryCaret() {
        return primaryCaret;
    }

    public EditorInfo setPrimaryCaret(Caret primaryCaret) {
        this.primaryCaret = primaryCaret;
        return this;
    }

    public DocumentTextInfo getDocumentTextInfo() {
        return documentTextInfo;
    }

    public EditorInfo setDocumentTextInfo(DocumentTextInfo documentTextInfo) {
        this.documentTextInfo = documentTextInfo;
        return this;
    }

    public SelectionInfo getSelectionInfo() {
        return selectionInfo;
    }

    public EditorInfo setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
        return this;
    }
}
