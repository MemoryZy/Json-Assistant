package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import com.intellij.openapi.actionSystem.DataContext;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JsonMinifyConversionProcessor extends JsonConversionProcessor {
    private JsonMinifyConversionProcessor(DataContext dataContext, EditorData editorData) {
        super(dataContext, editorData, false);
    }

    public static JsonMinifyConversionProcessor newProcessor(DataContext dataContext, EditorData editorData) {
        return new JsonMinifyConversionProcessor(dataContext, editorData);
    }
}
