package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.ConversionStrategy;
import cn.memoryzy.json.util.ConversionUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class UrlParamToJsonStrategy implements ConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return null != ConversionUtil.urlParamsToJson(text);
    }

    @Override
    public String convertToJson(String text) {
        return ConversionUtil.urlParamsToJson(text);
    }

}
