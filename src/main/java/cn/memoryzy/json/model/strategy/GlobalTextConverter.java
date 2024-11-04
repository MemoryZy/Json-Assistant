package cn.memoryzy.json.model.strategy;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.data.DocTextData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.SelectionData;
import cn.memoryzy.json.model.strategy.formats.JsonConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class GlobalTextConverter {

    // ----------------------------- 策略相关 ----------------------------- //

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


    // ------------------------------ 解析方法 ------------------------------ //

    /**
     * 从编辑器解析 JSON 文本并做对应处理（选中文本或全局文本）
     *
     * @param dataContext      上下文
     * @param editor           编辑器
     * @param needBeautify     是否需要美化（美化为 true，压缩为 false）
     * @param selectionMessage 选中文本转换成功的消息
     * @param globalMessage    全局文本转换成功的消息
     */
    public static void parseAndProcessJson(DataContext dataContext, Editor editor, boolean needBeautify, String selectionMessage, String globalMessage) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);

        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getJsonProcessors(dataContext, resolveEditor(editor), needBeautify);
        setHintMessage(processors, selectionMessage, globalMessage);

        // 执行转换
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String processedText = GlobalTextConverter.applyConversionProcessors(context, processors);

        if (StrUtil.isNotBlank(processedText)) {
            AbstractGlobalTextConversionProcessor processor = context.getProcessor();
            boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
            String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
            boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor.getDocument(), hasSelection, allowedFileTypeQualifiedNames);
            TextTransformUtil.applyProcessedTextToDocument(project, editor, processedText, processor, canWrite);
        }
    }


    /**
     * 从编辑器解析 JSON 文本
     *
     * @param dataContext  数据上下文
     * @param editor       编辑器
     * @param needBeautify 需要美化为 true，压缩为 false
     * @return JSON 文本
     */
    public static String parseJson(DataContext dataContext, Editor editor, boolean needBeautify) {
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getJsonProcessors(dataContext, resolveEditor(editor), needBeautify);
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        return GlobalTextConverter.applyConversionProcessors(context, processors);
    }

    /**
     * 从编辑器解析 JSON 文本（默认不关注格式化或压缩）
     *
     * @param dataContext 数据上下文
     * @param editor      编辑器
     * @return JSON 文本
     */
    public static String parseJson(DataContext dataContext, Editor editor) {
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getBeautifyJsonProcessors(dataContext, resolveEditor(editor));
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        return GlobalTextConverter.applyConversionProcessors(context, processors);
    }


    /**
     * 解析文本并验证是否为 JSON（从选中文本或全局文本中选取）
     *
     * @param project     项目
     * @param editor      编辑器
     * @param dataContext 上下文
     * @return 若为 JSON 则为true；反之为 false
     */
    public static boolean validateEditorJson(Project project, Editor editor, @NotNull DataContext dataContext) {
        if (project == null || editor == null) return false;
        EditorData editorData = resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getBeautifyJsonProcessors(dataContext, editorData);
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
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

    private static void setHintMessage(JsonConversionProcessor[] processors, String selectionMessage, String globalMessage) {
        for (JsonConversionProcessor processor : processors) {
            processor.getMessageData().setSelectionConvertSuccessMessage(selectionMessage).setGlobalConvertSuccessMessage(globalMessage);
        }
    }

}
