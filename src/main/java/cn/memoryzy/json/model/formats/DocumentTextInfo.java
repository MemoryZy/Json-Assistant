package cn.memoryzy.json.model.formats;

/**
 * 文档内文本信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class DocumentTextInfo {

    /**
     * 文档内的全部文本
     */
    private String documentText;

    /**
     * 当前选中的文本
     */
    private String selectedText;



    // ----------------------- GETTER/SETTER -----------------------

    public String getDocumentText() {
        return documentText;
    }

    public DocumentTextInfo setDocumentText(String documentText) {
        this.documentText = documentText;
        return this;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public DocumentTextInfo setSelectedText(String selectedText) {
        this.selectedText = selectedText;
        return this;
    }
}
