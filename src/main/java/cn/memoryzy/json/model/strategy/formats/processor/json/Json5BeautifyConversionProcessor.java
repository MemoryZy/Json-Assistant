package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class Json5BeautifyConversionProcessor extends Json5ConversionProcessor {
    private Json5BeautifyConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    public static Json5BeautifyConversionProcessor newProcessor(EditorData editorData) {
        return new Json5BeautifyConversionProcessor(editorData);
    }
}
