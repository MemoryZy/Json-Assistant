package cn.memoryzy.json.model.formats;

/**
 * 文本选择相关信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class SelectionInfo {

    /**
     * 检查当前是否选择了文本范围
     */
    private boolean hasSelection;

    /**
     * 所选文本范围在文档中的起始偏移量
     */
    private int startOffset;

    /**
     * 所选文本范围在文档中的结束偏移量
     */
    private int endOffset;



    // ----------------------- GETTER/SETTER -----------------------

    public boolean isHasSelection() {
        return hasSelection;
    }

    public SelectionInfo setHasSelection(boolean hasSelection) {
        this.hasSelection = hasSelection;
        return this;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public SelectionInfo setStartOffset(int startOffset) {
        this.startOffset = startOffset;
        return this;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public SelectionInfo setEndOffset(int endOffset) {
        this.endOffset = endOffset;
        return this;
    }

}
