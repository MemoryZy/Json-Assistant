package cn.memoryzy.json.model.strategy.formats.data;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;

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

    /**
     * 当前编辑器
     */
    private Editor editor;

    /**
     * 解析并保留注释（只限于 JSON5 格式）
     */
    private boolean parseComment = false;

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

    public Editor getEditor() {
        return editor;
    }

    public EditorData setEditor(Editor editor) {
        this.editor = editor;
        return this;
    }

    public boolean isParseComment() {
        return parseComment;
    }

    public EditorData setParseComment(boolean parseComment) {
        this.parseComment = parseComment;
        return this;
    }
}
