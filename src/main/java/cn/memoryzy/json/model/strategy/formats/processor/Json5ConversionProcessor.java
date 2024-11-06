package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.JsonConversionProcessor;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class Json5ConversionProcessor extends JsonConversionProcessor {

    public Json5ConversionProcessor(EditorData editorData, boolean needBeautify) {
        super(editorData, needBeautify);
    }

    @Override
    public boolean canConvert(String text) {
        return super.canConvert(text);
    }

    @Override
    public String convertToJson() {
        return super.convertToJson();
    }
}
