package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.JsonUtil;

/**
 * JSON 转 JSON 处理
 *
 * @author Memory
 * @since 2024/10/31
 */
public class JsonConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public String type() {
        return DataTypeConstant.JSON;
    }

    @Override
    public boolean canConvert(String text) {
        return JsonUtil.canResolveToJson(text);
    }

    @Override
    public String convertToJson(String text) {
        return JsonUtil.ensureJson(text);
    }

}
