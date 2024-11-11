package cn.memoryzy.json.model.strategy;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.DocTextData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.SelectionData;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class GlobalTextConverter {

    /**
     * 尝试不同的策略解析并转换编辑器文本
     *
     * @param context 上下文
     * @param editor  编辑器
     * @return 转换成功的 JSON 文本
     */
    public static String applyConversionProcessors(GlobalTextConversionProcessorContext context, Editor editor) {
        EditorData editorData = resolveEditor(editor);
        if (editorData == null) return null;
        return applyConversionProcessors(context, GlobalTextConversionProcessorContext.getProcessors(editorData));
    }

    /**
     * 尝试不同的策略解析并转换编辑器文本
     *
     * @param context    上下文
     * @param processors 处理器
     * @return 转换成功的 JSON 文本
     */
    public static String applyConversionProcessors(GlobalTextConversionProcessorContext context, AbstractGlobalTextConversionProcessor[] processors) {
        for (AbstractGlobalTextConversionProcessor processor : processors) {
            context.setProcessor(processor);
            String result = context.convert(processor.getEditorData());
            if (StrUtil.isNotBlank(result)) {
                return result;
            }
        }
        return null;
    }


    /**
     * 尝试不同的策略解析编辑器文本，并判断是否符合格式
     *
     * @param context 上下文
     * @param editor  编辑器
     * @return 符合为 true，反之为 false
     */
    public static boolean validateEditorText(GlobalTextConversionProcessorContext context, Editor editor) {
        EditorData editorData = resolveEditor(editor);
        if (editorData == null) return false;
        return validateEditorText(context, GlobalTextConversionProcessorContext.getProcessors(editorData));
    }

    /**
     * 尝试不同的策略解析编辑器文本，并判断是否符合格式
     *
     * @param context    上下文
     * @param processors 处理器
     * @return 符合为 true，反之为 false
     */
    public static boolean validateEditorText(GlobalTextConversionProcessorContext context, AbstractGlobalTextConversionProcessor[] processors) {
        for (AbstractGlobalTextConversionProcessor processor : processors) {
            context.setProcessor(processor);
            boolean matched = context.isMatched(processor.getEditorData());
            if (matched) return true;
        }

        return false;
    }


    /**
     * 解析编辑器文本
     *
     * @param editor 编辑器
     * @return left：解析完成的文本；right：编辑器相关信息
     */
    public static EditorData resolveEditor(Editor editor) {
        if (editor == null) return null;
        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = primaryCaret.getSelectionStart();
        int endOffset = primaryCaret.getSelectionEnd();

        String documentText = document.getText();
        String selectedText = document.getText(new TextRange(startOffset, endOffset));

        if (StrUtil.isBlank(documentText) && StrUtil.isBlank(selectedText)) return null;

        DocTextData docTextData = new DocTextData()
                .setSelectedText(selectedText)
                .setDocumentText(documentText);

        SelectionData selectionData = new SelectionData()
                .setHasSelection(StrUtil.isNotBlank(selectedText))
                .setStartOffset(startOffset)
                .setEndOffset(endOffset);

        return new EditorData().setPrimaryCaret(primaryCaret).setDocTextData(docTextData).setSelectionData(selectionData);
    }

}
