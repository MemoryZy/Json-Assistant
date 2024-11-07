package cn.memoryzy.json.model.strategy;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.Json5ConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.JsonConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.util.TextTransformUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class GlobalJsonConverter {

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

        JsonConversionProcessor[] processors =
                GlobalTextConversionProcessorContext.getJsonProcessors(
                        dataContext, GlobalTextConverter.resolveEditor(editor), needBeautify);

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
        JsonConversionProcessor[] processors =
                GlobalTextConversionProcessorContext.getJsonProcessors(
                        dataContext, GlobalTextConverter.resolveEditor(editor), needBeautify);

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
        JsonConversionProcessor[] processors =
                GlobalTextConversionProcessorContext.getBeautifyJsonProcessors(
                        dataContext, GlobalTextConverter.resolveEditor(editor));

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        return GlobalTextConverter.applyConversionProcessors(context, processors);
    }


    /**
     * 解析文本并验证是否为 JSON/JSON5（从选中文本或全局文本中选取）
     *
     * @param project     项目
     * @param editor      编辑器
     * @param dataContext 上下文
     * @return 若为 JSON 则为true；反之为 false
     */
    public static boolean validateEditorAllJson(Project project, Editor editor, @NotNull DataContext dataContext) {
        if (project == null || editor == null) return false;
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = GlobalTextConversionProcessorContext.getBeautifyJsonProcessors(dataContext, editorData);
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
    }

    /**
     * 解析文本并验证是否为 JSON5（从选中文本或全局文本中选取）
     *
     * @param project     项目
     * @param editor      编辑器
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
     * @param project     项目
     * @param editor      编辑器
     * @param dataContext 上下文
     * @return 若为 JSON 则为true；反之为 false
     */
    public static boolean validateEditorJson(Project project, Editor editor, @NotNull DataContext dataContext) {
        if (project == null || editor == null) return false;
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (editorData == null) return false;

        JsonConversionProcessor[] processors = {new JsonConversionProcessor(dataContext, editorData, true)};
        return GlobalTextConverter.validateEditorText(new GlobalTextConversionProcessorContext(), processors);
    }


    private static void setHintMessage(JsonConversionProcessor[] processors, String selectionMessage, String globalMessage) {
        for (JsonConversionProcessor processor : processors) {
            processor.getMessageData().setSelectionConvertSuccessMessage(selectionMessage).setGlobalConvertSuccessMessage(globalMessage);
        }
    }
}
