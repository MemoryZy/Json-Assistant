package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.Json5Util;

/**
 * @author Memory
 * @since 2024/11/12
 */
public class Json5ConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return Json5Util.isJson5(text);
    }

    @Override
    public String convertToJson(String text) {
        return Json5Util.formatJson5(text);
    }

}
