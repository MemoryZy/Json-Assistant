package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.XmlUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class XmlConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public String type() {
        return DataTypeConstant.XML;
    }

    @Override
    public boolean canConvert(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson(String text) {
        return XmlUtil.toJson(text);
    }

}
