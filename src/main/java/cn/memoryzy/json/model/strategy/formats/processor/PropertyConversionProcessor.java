package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class PropertyConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public PropertyConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) throws Exception {
        return false;
    }

    @Override
    public String convertToJson() throws Exception {
        return null;
    }

}
