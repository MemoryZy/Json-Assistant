package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.ConversionStrategy;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class UrlParamToJsonStrategy implements ConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return false;
    }

    @Override
    public String convertToJson(String text) {
        return null;
    }

}
