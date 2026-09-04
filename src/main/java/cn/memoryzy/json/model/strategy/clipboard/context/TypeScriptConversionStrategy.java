package cn.memoryzy.json.model.strategy.clipboard.context;

import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.util.TypeScriptUtil;

/**
 * @author Memory
 * @since 2026/9/4
 */
public class TypeScriptConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public String type() {
        return DataTypeConstant.TYPE_SCRIPT;
    }

    @Override
    public boolean canConvert(String text) throws Exception {
        return TypeScriptUtil.canConvert(text);
    }

    @Override
    public String convertToJson(String text) throws Exception {
        return TypeScriptUtil.convertToJson(text);
    }
}