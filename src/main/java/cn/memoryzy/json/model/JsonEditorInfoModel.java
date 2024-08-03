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


    public JsonEditorInfoModel(Editor editor) {
        Document document = editor.getDocument();
        this.primaryCaret = editor.getCaretModel().getPrimaryCaret();
        this.startOffset = this.primaryCaret.getSelectionStart();
        this.endOffset = this.primaryCaret.getSelectionEnd();
        String selectText = document.getText(new TextRange(this.startOffset, this.endOffset));
        this.jsonContent = (JsonUtil.isJsonStr(selectText)) ? selectText : JsonUtil.extractJsonStr(selectText);

        this.isSelectedText = true;
        if (StrUtil.isBlank(this.jsonContent)) {
            this.isSelectedText = false;
            String documentText = document.getText();
            this.jsonContent = (JsonUtil.isJsonStr(documentText)) ? documentText : JsonUtil.extractJsonStr(documentText);
        }

        this.isJsonStr = StrUtil.isNotBlank(this.jsonContent);
    }
}
