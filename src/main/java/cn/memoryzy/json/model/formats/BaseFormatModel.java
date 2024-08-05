package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.editor.Caret;

/**
 * @author Memory
 * @since 2024/8/4
 */
public abstract class BaseFormatModel {

    /**
     * 是否为通过选中内容得出的文本（如果是选中得到的文本，则为 true，反之为 false）
     */
    private Boolean isSelected;

    /**
     * 选中文本的开始偏移量
     */
    private int startOffset;

    /**
     * 选中文本的结束偏移量
     */
    private int endOffset;

    /**
     * 当前编辑器中主要的光标
     */
    private Caret primaryCaret;

    /**
     * 文本
     */
    private String content;

    public BaseFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.primaryCaret = primaryCaret;
    }

    public BaseFormatModel(Boolean isSelected, int startOffset, int endOffset, Caret primaryCaret, String content) {
        this.isSelected = isSelected;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.primaryCaret = primaryCaret;
        this.content = content;
    }

    public abstract boolean isConformFormat(String text);

    public abstract String convertToJson();

    public abstract String getActionName();

    public abstract String getActionDescription();

    public abstract String getSelectHint();

    public abstract String getDefaultHint();



    public static void fillModel(String selectText, String documentText, BaseFormatModel model) {
        String text = model.isConformFormat(selectText) ? selectText.trim() : null;

        boolean isSelected = true;
        if (StrUtil.isBlank(text)) {
            isSelected = false;
            if (model.isConformFormat(documentText)) {
                text = documentText.trim();
            }
        }

        model.setContent(StrUtil.isNotBlank(text) ? text : null).setSelectedText(isSelected);
    }


    // -----------------------------------


    public Boolean getSelectedText() {
        return isSelected;
    }

    public BaseFormatModel setSelectedText(Boolean selectedText) {
        isSelected = selectedText;
        return this;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public BaseFormatModel setStartOffset(int startOffset) {
        this.startOffset = startOffset;
        return this;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public BaseFormatModel setEndOffset(int endOffset) {
        this.endOffset = endOffset;
        return this;
    }

    public Caret getPrimaryCaret() {
        return primaryCaret;
    }

    public BaseFormatModel setPrimaryCaret(Caret primaryCaret) {
        this.primaryCaret = primaryCaret;
        return this;
    }

    public String getContent() {
        return content;
    }

    public BaseFormatModel setContent(String content) {
        this.content = content;
        return this;
    }

}
