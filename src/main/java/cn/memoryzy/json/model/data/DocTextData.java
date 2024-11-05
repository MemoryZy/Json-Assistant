package cn.memoryzy.json.model.data;

/**
 * 文档内文本信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class DocTextData {

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

    public DocTextData setDocumentText(String documentText) {
        this.documentText = documentText;
        return this;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public DocTextData setSelectedText(String selectedText) {
        this.selectedText = selectedText;
        return this;
    }
}
