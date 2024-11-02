package cn.memoryzy.json.model.strategy.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.formats.DocumentTextInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.SelectionInfo;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

/**
 * @author Memory
 * @since 2024/11/2
 */
public class ConversionProcessorContext {

    private AbstractConversionProcessor processor;

    public AbstractConversionProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(AbstractConversionProcessor processor) {
        this.processor = processor;
    }

    /**
     * 代理处理器转换文本
     *
     * @param editorInfo 编辑器信息
     * @return 转换成功的 JSON 文本
     */
    public String convert(EditorInfo editorInfo) {
        SelectionInfo selectionInfo = editorInfo.getSelectionInfo();
        DocumentTextInfo documentTextInfo = editorInfo.getDocumentTextInfo();
        String selectedText = documentTextInfo.getSelectedText();
        String documentText = documentTextInfo.getDocumentText();

        String result;
        if (selectionInfo.isHasSelection()) {
            result = processor.convert(selectedText);
            if (StrUtil.isNotBlank(result)) {
                processor.setTextResolveStatus(TextResolveStatus.SELECTED_SUCCESS);
                return result;
            } else {
                // 如果选择了文本，但是没通过匹配，则跳过，选择下一个处理器
                processor.setTextResolveStatus(TextResolveStatus.RESOLVE_FAILED);
                return null;
            }
        }

        result = processor.convert(documentText);
        processor.setTextResolveStatus(StrUtil.isNotBlank(result)
                ? TextResolveStatus.GLOBAL_SUCCESS
                : TextResolveStatus.RESOLVE_FAILED);

        return result;
    }

    /**
     * 尝试不同的策略解析编辑器文本
     *
     * @param context 上下文
     * @param editor  编辑器
     * @return 转换成功的 JSON 文本
     */
    public static String applyProcessors(ConversionProcessorContext context, Editor editor) {
        EditorInfo editorInfo = resolveEditor(editor);
        if (editorInfo == null) return null;

        // 策略处理器
        AbstractConversionProcessor[] processors = {
                new XmlProcessor(editorInfo)
                // TODO 待实现其他的处理器
        };

        return applyProcessors(context, processors, editorInfo);
    }

    public static String applyProcessors(ConversionProcessorContext context, AbstractConversionProcessor[] processors, EditorInfo editorInfo) {
        for (AbstractConversionProcessor processor : processors) {
            context.setProcessor(processor);
            String result = context.convert(editorInfo);
            if (StrUtil.isNotBlank(result)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 解析编辑器文本
     *
     * @param editor 编辑器
     * @return left：解析完成的文本；right：编辑器相关信息
     */
    private static EditorInfo resolveEditor(Editor editor) {
        if (editor == null) return null;
        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();

        String documentText = document.getText();
        String selectedText = document.getText(new TextRange(startOffset, endOffset));

        if (StrUtil.isBlank(documentText) && StrUtil.isBlank(selectedText)) return null;

        DocumentTextInfo documentTextInfo = new DocumentTextInfo()
                .setSelectedText(selectedText)
                .setDocumentText(documentText);

        SelectionInfo selectionInfo = new SelectionInfo()
                .setHasSelection(StrUtil.isNotBlank(selectedText))
                .setStartOffset(startOffset)
                .setEndOffset(endOffset);

        return new EditorInfo().setPrimaryCaret(primaryCaret).setDocumentTextInfo(documentTextInfo).setSelectionInfo(selectionInfo);
    }

}
