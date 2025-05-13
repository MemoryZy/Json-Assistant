package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JsonMinifyConversionProcessor extends JsonConversionProcessor {
    private JsonMinifyConversionProcessor(EditorData editorData) {
        super(editorData, false);
    }

    public static JsonMinifyConversionProcessor newProcessor(EditorData editorData) {
        return new JsonMinifyConversionProcessor(editorData);
    }
}
