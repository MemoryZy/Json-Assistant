package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.data.EditorData;
import com.intellij.openapi.actionSystem.DataContext;

/**
 * @author Memory
 * @since 2024/11/8
 */
public class JsonBeautifyConversionProcessor extends JsonConversionProcessor {
    private JsonBeautifyConversionProcessor(DataContext dataContext, EditorData editorData) {
        super(dataContext, editorData, true);
    }

    public static JsonBeautifyConversionProcessor newProcessor(DataContext dataContext, EditorData editorData) {
        return new JsonBeautifyConversionProcessor(dataContext, editorData);
    }
}
