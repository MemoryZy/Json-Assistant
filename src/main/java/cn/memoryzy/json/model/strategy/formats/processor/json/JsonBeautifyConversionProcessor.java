package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JsonBeautifyConversionProcessor extends JsonConversionProcessor {
    private JsonBeautifyConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    public static JsonBeautifyConversionProcessor newProcessor(EditorData editorData) {
        return new JsonBeautifyConversionProcessor(editorData);
    }
}
