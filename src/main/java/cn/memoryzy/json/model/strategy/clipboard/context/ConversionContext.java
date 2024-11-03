package cn.memoryzy.json.model.strategy.clipboard.context;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.clipboard.*;
import cn.memoryzy.json.service.persistent.EditorOptionsPersistentState;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;

/**
 * 维护成功的策略
 *
 * @author Memory
 * @since 2024/10/31
 */
public class ConversionContext {

    private static final Logger LOG = Logger.getInstance(ConversionContext.class);
    private static final EditorOptionsPersistentState STATE = EditorOptionsPersistentState.getInstance();

    private ConversionStrategy strategy;

    public ConversionStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ConversionStrategy strategy) {
        this.strategy = strategy;
    }


    public String convert(String text) {
        try {
            if (strategy != null && strategy.canConvert(text)) {
                return strategy.convertToJson(text);
            }
        } catch (Throwable ignored) {
        }

        return null;
    }


    public static String applyStrategies(ConversionContext context, String text) {
        List<ConversionStrategy> conversionStrategies = Lists.newArrayList(new JsonToJsonStrategy());

        if (STATE.recognizeXmlFormat) {
            conversionStrategies.add(new XmlToJsonStrategy());
        }

        if (STATE.recognizeYamlFormat) {
            conversionStrategies.add(new YamlToJsonStrategy());
        }

        if (STATE.recognizeTomlFormat) {
            conversionStrategies.add(new TomlToJsonStrategy());
        }

        if (STATE.recognizeUrlParamFormat) {
            conversionStrategies.add(new UrlParamToJsonStrategy());
        }

        // 依次尝试不同的转换策略
        return applyStrategies(context, conversionStrategies, text);
    }

    private static String applyStrategies(ConversionContext context, List<ConversionStrategy> strategies, String text) {
        for (ConversionStrategy strategy : strategies) {
            context.setStrategy(strategy);
            String result = context.convert(text);
            if (StrUtil.isNotBlank(result)) {
                return result;
            }
        }

        return null;
    }

}
