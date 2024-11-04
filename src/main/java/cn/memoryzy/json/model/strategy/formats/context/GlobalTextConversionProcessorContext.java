package cn.memoryzy.json.model.strategy.formats.context;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.data.DocTextData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.SelectionData;
import cn.memoryzy.json.model.strategy.formats.JsonConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.processor.TomlConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.processor.XmlConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.processor.YamlConversionProcessor;
import com.intellij.openapi.actionSystem.DataContext;

/**
 * @author Memory
 * @since 2024/11/2
 */
public class GlobalTextConversionProcessorContext {

    private AbstractGlobalTextConversionProcessor processor;

    public AbstractGlobalTextConversionProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(AbstractGlobalTextConversionProcessor processor) {
        this.processor = processor;
    }

    /**
     * 代理处理器转换文本
     *
     * @param editorData 编辑器信息
     * @return 转换成功的 JSON 文本
     */
    public String convert(EditorData editorData) {
        SelectionData selectionData = editorData.getSelectionData();
        DocTextData docTextData = editorData.getDocTextData();
        String selectedText = docTextData.getSelectedText();
        String documentText = docTextData.getDocumentText();

        String result;
        if (selectionData.isHasSelection()) {
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
     * 文本是否匹配成功（在转换方法 {@link GlobalTextConversionProcessorContext#convert(EditorData)} 前执行）
     *
     * @param editorData 编辑器信息
     * @return 文本匹配成功为 true，反之为 false
     */
    public boolean isMatched(EditorData editorData) {
        SelectionData selectionData = editorData.getSelectionData();
        DocTextData docTextData = editorData.getDocTextData();
        String selectedText = docTextData.getSelectedText();
        String documentText = docTextData.getDocumentText();

        try {
            if (selectionData.isHasSelection()) {
                // 如果选择了文本，通过匹配则返回 true；若没通过匹配，则跳过，选择下一个处理器
                return processor.canConvert(selectedText);
            }

            return processor.canConvert(documentText);
        } catch (Exception e) {
            return false;
        }
    }


    // ------------------------------ Static Method ------------------------------ //

    /**
     * 获取策略处理器列表（其他格式转 JSON）
     *
     * @param editorData 编辑器信息
     * @return 策略处理器列表
     */
    public static AbstractGlobalTextConversionProcessor[] getProcessors(EditorData editorData) {
        return new AbstractGlobalTextConversionProcessor[]{
                new XmlConversionProcessor(editorData),
                new YamlConversionProcessor(editorData),
                new TomlConversionProcessor(editorData)
                // 待实现其他的处理器
        };
    }

    /**
     * 获取 JSON 处理器列表（转换出的结果会被美化）
     *
     * @param dataContext 数据上下文
     * @param editorData  编辑器信息
     * @return 美化 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getBeautifyJsonProcessors(DataContext dataContext, EditorData editorData) {
        return getJsonProcessors(dataContext, editorData, true);
    }

    /**
     * 获取 JSON 处理器列表（转换出的结果会被压缩）
     *
     * @param dataContext 数据上下文
     * @param editorData  编辑器信息
     * @return 压缩 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getCompressJsonProcessors(DataContext dataContext, EditorData editorData) {
        return getJsonProcessors(dataContext, editorData, false);
    }

    /**
     * 获取通用 JSON 处理器列表
     *
     * @param dataContext  数据上下文
     * @param editorData   编辑器信息
     * @param needBeautify 是否需要美化
     * @return 通用 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getJsonProcessors(DataContext dataContext, EditorData editorData, boolean needBeautify) {
        return new JsonConversionProcessor[]{
                new JsonConversionProcessor(dataContext, editorData, needBeautify)
                // 待添加其他格式，例如 JSON5
        };
    }


}
