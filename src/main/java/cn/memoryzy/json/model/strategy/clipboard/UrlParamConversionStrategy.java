package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.TextTransformUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class UrlParamConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return null != TextTransformUtil.urlParamsToJson(text);
    }

    @Override
    public String convertToJson(String text) {
        return TextTransformUtil.urlParamsToJson(text);
    }

}
