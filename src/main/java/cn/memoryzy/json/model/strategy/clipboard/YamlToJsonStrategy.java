package cn.memoryzy.json.model.strategy.clipboard;

import cn.memoryzy.json.model.strategy.ConversionStrategy;
import cn.memoryzy.json.util.YamlUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public class YamlToJsonStrategy implements ConversionStrategy {

    @Override
    public boolean canConvert(String text) {
        // 不处理多文档
        return YamlUtil.isSingleYamlDocument(text);
    }

    @Override
    public String convertToJson(String text) {
        return YamlUtil.toJson(text);
    }

}
