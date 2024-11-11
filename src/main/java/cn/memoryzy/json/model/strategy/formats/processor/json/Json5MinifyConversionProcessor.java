package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class Json5MinifyConversionProcessor extends Json5ConversionProcessor {
    private Json5MinifyConversionProcessor(EditorData editorData) {
        super(editorData, false);
    }

    public static Json5MinifyConversionProcessor newProcessor(EditorData editorData) {
        return new Json5MinifyConversionProcessor(editorData);
    }
}
