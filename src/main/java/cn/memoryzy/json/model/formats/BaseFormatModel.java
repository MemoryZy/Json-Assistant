package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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

    /**
     * 文件类型
     */
    private String fileTypeClassName;

    public BaseFormatModel(int startOffset, int endOffset, Caret primaryCaret, String fileTypeClassName) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.primaryCaret = primaryCaret;
        this.fileTypeClassName = fileTypeClassName;
    }

    public BaseFormatModel(Boolean isSelected, int startOffset, int endOffset, Caret primaryCaret, String content, String fileTypeClassName) {
        this.isSelected = isSelected;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.primaryCaret = primaryCaret;
        this.content = content;
        this.fileTypeClassName = fileTypeClassName;
    }

    public abstract boolean isConformFormat(String text);

    public abstract String convertToJson();

    public abstract String getActionName();

    public abstract String getActionDescription();

    public abstract Icon getActionIcon();

    public abstract String getSelectHint();

    public abstract String getDefaultHint();

    /**
     * 执行转换前的处理，若要更改转换的文本，则调用setContent()方法
     */
    public abstract void preprocessing();


    public static void prepareModel(Project project, @NotNull Document document, String selectText, String documentText, BaseFormatModel model) {
        String text = null;
        boolean isSelected = false;

        // 选中了文本，那就判断这段文本
        if (StrUtil.isNotBlank(selectText)) {
            text = model.isConformFormat(selectText) ? selectText : null;
            if (StrUtil.isNotBlank(text)) {
                isSelected = true;
            } else {
                throw new RuntimeException();
            }
        } else {
            if (model.isConformFormat(documentText)){
                text = documentText;
            }

            if (StrUtil.isBlank(text)) {
                return;
            }
        }

        model.setContent(text).setSelectedText(isSelected);
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

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getFileTypeClassName() {
        return fileTypeClassName;
    }

    public void setFileTypeClassName(String fileTypeClassName) {
        this.fileTypeClassName = fileTypeClassName;
    }
}
