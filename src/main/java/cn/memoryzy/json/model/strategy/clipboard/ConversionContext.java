package cn.memoryzy.json.model.strategy.clipboard;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.ConversionStrategy;
import com.intellij.openapi.diagnostic.Logger;

/**
 * 维护成功的策略
 *
 * @author Memory
 * @since 2024/10/31
 */
public class ConversionContext {

    private static final Logger LOG = Logger.getInstance(ConversionContext.class);

    // TODO 用策略模式改造格式转换那块
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
        // 依次尝试不同的转换策略
        ConversionStrategy[] strategies = {
                new JsonToJsonStrategy(),
                new XmlToJsonStrategy(),
                new YamlToJsonStrategy(),
                new TomlToJsonStrategy(),
                new UrlParamToJsonStrategy()
        };

        return applyStrategies(context, strategies, text);
    }

    private static String applyStrategies(ConversionContext context, ConversionStrategy[] strategies, String text) {
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
