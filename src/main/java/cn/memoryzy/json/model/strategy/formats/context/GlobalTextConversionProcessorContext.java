package cn.memoryzy.json.model.strategy.formats.context;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.strategy.formats.data.DocTextData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.SelectionData;
import cn.memoryzy.json.model.strategy.formats.processor.*;
import cn.memoryzy.json.model.strategy.formats.processor.json.*;

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
                new TomlConversionProcessor(editorData),
                new UrlParamConversionProcessor(editorData),
                new PropertiesConversionProcessor(editorData),
                // 待实现其他的处理器
        };
    }

    /**
     * 获取 JSON 处理器列表（转换出的结果会被美化）
     *
     * @param editorData 编辑器信息
     * @return 美化 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getBeautifyAllJsonProcessors(EditorData editorData) {
        return new JsonConversionProcessor[]{
                JsonBeautifyConversionProcessor.newProcessor(editorData),
                Json5BeautifyConversionProcessor.newProcessor(editorData)
        };
    }

    /**
     * 获取 JSON 处理器列表（转换出的结果会被压缩）
     *
     * @param editorData 编辑器信息
     * @return 压缩 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getCompressAllJsonProcessors(EditorData editorData) {
        return new JsonConversionProcessor[]{
                JsonMinifyConversionProcessor.newProcessor(editorData),
                Json5MinifyConversionProcessor.newProcessor(editorData)
        };
    }

    /**
     * 获取 JSON 处理器列表（转换出的结果不变化）
     *
     * @param editorData 编辑器信息
     * @return 压缩 JSON 处理器列表
     */
    public static JsonConversionProcessor[] getOriginalAllJsonProcessors(EditorData editorData) {
        return new JsonConversionProcessor[]{
                new JsonConversionProcessor(editorData, null),
                new Json5ConversionProcessor(editorData, null)
        };
    }

}
