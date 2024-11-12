package cn.memoryzy.json.model.strategy.formats.processor.json;

import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.util.Json5Util;

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
        return Json5Util.isJson5(text);
    }

    @Override
    public String convertToJson() {
        return getContent();
    }

    @Override
    public String postprocessing(String text) {
        return isNeedBeautify() ? Json5Util.formatJson5(text) : Json5Util.compressJson5(text);
    }
}
