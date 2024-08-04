package cn.memoryzy.json.model;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class JsonEditorInfoModel {

    /**
     * 是否为通过选中内容得出的 JSON 文本（如果是选中得到的 JSON 文本，则为 true，反之为 false）
     */
    public Boolean isSelectedText;

    /**
     * 选中 Json 文本的开始偏移量
     */
    public int startOffset;

    /**
     * 选中 Json 文本的结束偏移量
     */
    public int endOffset;

    /**
     * 当前编辑器中主要的光标
     */
    public Caret primaryCaret;

    /**
     * 是否为 JSON 格式文本
     */
    public boolean isJsonStr;

    /**
     * Json 文本
     */
    public String jsonContent;

    public static JsonEditorInfoModel of(Editor editor) {
        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();
        String selectText = document.getText(new TextRange(startOffset, endOffset));
        String jsonContent = (JsonUtil.isJsonStr(selectText)) ? selectText : JsonUtil.extractJsonStr(selectText);

        boolean isSelectedText = true;
        if (StrUtil.isBlank(jsonContent)) {
            isSelectedText = false;
            String documentText = document.getText();
            jsonContent = (JsonUtil.isJsonStr(documentText)) ? documentText : JsonUtil.extractJsonStr(documentText);
        }

        return new JsonEditorInfoModel(isSelectedText, startOffset, endOffset, primaryCaret, StrUtil.isNotBlank(jsonContent), jsonContent);
    }


    public JsonEditorInfoModel(Boolean isSelectedText, int startOffset, int endOffset, Caret primaryCaret, boolean isJsonStr, String jsonContent) {
        this.isSelectedText = isSelectedText;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.primaryCaret = primaryCaret;
        this.isJsonStr = isJsonStr;
        this.jsonContent = jsonContent;
    }

    public Boolean getSelectedText() {
        return isSelectedText;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public Caret getPrimaryCaret() {
        return primaryCaret;
    }

    public boolean isJsonStr() {
        return isJsonStr;
    }

    public String getJsonContent() {
        return jsonContent;
    }
}
