package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.TomlUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class TomlConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return TomlUtil.isToml(text);
    }

    @Override
    public String convertToJson(String text) {
        return TomlUtil.toJson(text);
    }

}
