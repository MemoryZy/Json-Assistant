package cn.memoryzy.json.model.strategy;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionContext;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;

import java.util.List;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class ClipboardTextConverter {

    public static String applyConversionStrategies(ClipboardTextConversionContext context, String text) {
        // 依次尝试不同的转换策略
        return applyConversionStrategies(context, ClipboardTextConversionContext.getProcessors(), text);
    }

    private static String applyConversionStrategies(ClipboardTextConversionContext context, List<ClipboardTextConversionStrategy> strategies, String text) {
        for (ClipboardTextConversionStrategy strategy : strategies) {
            context.setStrategy(strategy);
            String result = context.convert(text);
            if (StrUtil.isNotBlank(result)) {
                return result;
            }
        }

        return null;
    }

}
