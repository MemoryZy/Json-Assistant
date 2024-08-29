package cn.memoryzy.json.models.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

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

    public abstract Icon getActionIcon();

    public abstract String getSelectHint();

    public abstract String getDefaultHint();

    public abstract FileType getFileType();


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
            // 没选择文本，那就判断是否为指定文件类型，如果不是就不显示
            FileType fileType = PlatformUtil.getDocumentFileType(project, document);
            if (Objects.equals(fileType, model.getFileType())) {
                text = model.isConformFormat(documentText) ? documentText : null;
            }

            if (StrUtil.isBlank(text)) {
                throw new RuntimeException();
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

}
