package cn.memoryzy.json.model.strategy;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.processor.json.Json5ConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.processor.json.JsonConversionProcessor;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class GlobalJsonConverter {

    // --------------------------------- Json本身处理方法 --------------------------------- //

    /**
     * 从编辑器解析 JSON 文本并做对应处理（选中文本或全局文本）
     * <p style="color: blue;">方法适用于 JSON 本身的处理（如 格式化、压缩等），不适用于转换类型</p>
     *
     * @param dataContext      上下文
     * @param editor           编辑器
     * @param needBeautify     是否需要美化（美化为 true，压缩为 false）
     * @param selectionMessage 选中文本转换成功的消息
     * @param globalMessage    全局文本转换成功的消息
     */
    public static void parseAndProcessJson(DataContext dataContext, Editor editor, boolean needBeautify, String selectionMessage, String globalMessage) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (Objects.isNull(editorData)) {
            return;
        }

        JsonConversionProcessor[] processors = needBeautify
                ? GlobalTextConversionProcessorContext.getBeautifyAllJsonProcessors(editorData)
                : GlobalTextConversionProcessorContext.getCompressAllJsonProcessors(editorData);

        setHintMessage(processors, selectionMessage, globalMessage);

        // 执行转换
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String processedText = GlobalTextConverter.applyConversionProcessors(context, processors);

        if (StrUtil.isNotBlank(processedText)) {
            AbstractGlobalTextConversionProcessor processor = context.getProcessor();
            boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
            String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
            boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor, hasSelection, allowedFileTypeQualifiedNames);
            TextTransformUtil.applyProcessedTextToDocument(project, editor, processedText, processor, canWrite);
        }
    }


    // --------------------------------- Json与Json5互相转换的处理方法 --------------------------------- //


    /**
     * 从编辑器解析 Json/Json5 文本，将其做指定操作，接着将处理完成的文本应用至文档内
     * <p style="color: blue;">方法适用于 JSON 与 JSON5 互相转换</p>
     *
     * @param dataContext      数据上下文
     * @param editor           编辑器
     * @param converter        转换函数
     * @param selectionMessage 选中文本转换成功的消息
     * @param globalMessage    全局文本转换成功的消息
     */
    public static void convertBetweenJsonAndJson5(DataContext dataContext, Editor editor, Function<String, String> converter, String selectionMessage, String globalMessage) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String processedText = converter.apply(parseJson(context, editor));

        if (StrUtil.isNotBlank(processedText)) {
            AbstractGlobalTextConversionProcessor processor = context.getProcessor();
            setHintMessage(processor, selectionMessage, globalMessage);

            boolean hasSelection = processor.getEditorData().getSelectionData().isHasSelection();
            String[] allowedFileTypeQualifiedNames = processor.getFileTypeData().getAllowedFileTypeQualifiedNames();
            boolean canWrite = TextTransformUtil.canWriteToDocument(dataContext, editor, hasSelection, allowedFileTypeQualifiedNames);
            TextTransformUtil.applyProcessedTextToDocument(project, editor, processedText, processor, canWrite);
        }
    }


    // --------------------------------- 单纯解析出Json的方法 --------------------------------- //


    /**
     * 从编辑器解析 JSON 文本
     *
     * @param editor       编辑器
     * @param needBeautify 需要美化为 true，压缩为 false
     * @return JSON 文本
     */
    public static String parseJson(Editor editor, boolean needBeautify) {
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (Objects.isNull(editorData)) return "";

        JsonConversionProcessor[] processors = needBeautify
                ? GlobalTextConversionProcessorContext.getBeautifyAllJsonProcessors(editorData)
                : GlobalTextConversionProcessorContext.getCompressAllJsonProcessors(editorData);

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        return parseJson(context, processors);
    }

    /**
     * 从编辑器解析 JSON 文本（默认不关注格式化或压缩）
     *
     * @param editor 编辑器
     * @return JSON 文本
     */
    public static String parseJson(Editor editor) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        return parseJson(context, editor);
    }

    public static String parseJson(GlobalTextConversionProcessorContext context, Editor editor) {
        return parseJson(context, GlobalTextConverter.resolveEditor(editor));
    }

    public static String parseJson(GlobalTextConversionProcessorContext context, EditorData editorData) {
        if (Objects.isNull(editorData)) return null;
        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getBeautifyAllJsonProcessors(editorData);
        return GlobalTextConverter.applyConversionProcessors(context, processors);
    }

    public static String parseJson(GlobalTextConversionProcessorContext context, JsonConversionProcessor[] processors) {
        return GlobalTextConverter.applyConversionProcessors(context, processors);
    }


    // -------------------------------------------------------------------------


    /**
     * 解析文本并验证是否为 JSON/JSON5（从选中文本或全局文本中选取）
     *
     * @param project 项目
     * @param editor  编辑器
     * @return 若为 JSON 则为true；反之为 false
     */
    public static boolean validateEditorAllJson(Project project, Editor editor) {
        if (project == null || editor == null) return false;
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getBeautifyAllJsonProcessors(editorData);
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
    }

    /**
     * 解析文本并验证是否为 JSON5（从选中文本或全局文本中选取）
     *
     * @param project 项目
     * @param editor  编辑器
     * @return 若为 JSON5 则为true；反之为 false
     */
    public static boolean validateEditorJson5(Project project, Editor editor) {
        if (project == null || editor == null) return false;
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = {new Json5ConversionProcessor(editorData, true)};
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
    }


    /**
     * 解析文本并验证是否为 JSON（从选中文本或全局文本中选取）
     *
     * @param project 项目
     * @param editor  编辑器
     * @return 若为 JSON 则为true；反之为 false
     */
    public static boolean validateEditorJson(Project project, Editor editor) {
        if (project == null || editor == null) return false;
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = {new JsonConversionProcessor(editorData, true)};
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
    }


    /**
     * 判断解析结果是否为 JSON
     * <p style="color: blue;">这是针对 {@link GlobalTextConversionProcessorContext#getProcessor()} 方法返回值来判断的</p>
     * <p style="color: blue;">因为现在分为了 JSON 和 JSON5，并且此方法也只有两个选择，非 JSON 即 JSON5</p>
     *
     * @param processor 处理器
     * @return true 为 JSON；反之为 JSON5
     */
    public static boolean isValidJson(AbstractGlobalTextConversionProcessor processor) {
        return !(processor instanceof Json5ConversionProcessor);
    }


    // -------------------------------------------------------------------------


    public static void setHintMessage(AbstractGlobalTextConversionProcessor[] processors, String selectionMessage, String globalMessage) {
        for (AbstractGlobalTextConversionProcessor processor : processors) {
            setHintMessage(processor, selectionMessage, globalMessage);
        }
    }

    public static void setHintMessage(AbstractGlobalTextConversionProcessor processor, String selectionMessage, String globalMessage) {
        processor.getMessageData().setSelectionConvertSuccessMessage(selectionMessage).setGlobalConvertSuccessMessage(globalMessage);
    }
}
