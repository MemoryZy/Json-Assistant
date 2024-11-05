package cn.memoryzy.json.model.strategy.clipboard;

import cn.hutool.core.util.StrUtil;
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
    public boolean canConvert(String text) {
        return JsonUtil.isJsonStr(text) || StrUtil.isNotBlank(JsonUtil.extractJsonStr(text));
    }

    @Override
    public String convertToJson(String text) {
        if (JsonUtil.isJsonStr(text)) {
            return text;
        }

        text = JsonUtil.extractJsonStr(text);
        return StrUtil.isNotBlank(text) ? text : null;
    }

}
