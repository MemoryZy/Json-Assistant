package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.ConversionStrategy;
import cn.memoryzy.json.util.XmlUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class XmlToJsonStrategy implements ConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson(String text) {
        return XmlUtil.toJson(text);
    }

}
