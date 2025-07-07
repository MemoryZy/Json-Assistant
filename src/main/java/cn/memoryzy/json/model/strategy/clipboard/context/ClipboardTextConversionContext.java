package cn.memoryzy.json.model.strategy.clipboard.context;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.DataFormatType;
import cn.memoryzy.json.model.strategy.clipboard.*;
import cn.memoryzy.json.service.persistent.ToolWindowSettings;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * 维护成功的策略
 *
 * @author Memory
 * @since 2024/10/31
 */
public class ClipboardTextConversionContext {

    private static final EditorBehaviorState STATE = ToolWindowSettings.getInstance().getBehaviorState();

    private ClipboardTextConversionStrategy strategy;

    public String convert(String text) {
        try {
            text = StrUtil.trim(text);
            if (strategy != null && strategy.canConvert(text)) {
                return strategy.convertToJson(text);
            }
        } catch (Throwable ignored) {
        }

        return null;
    }


    /**
     * 获取策略处理器列表（其他格式转 JSON）
     *
     * @return 策略处理器列表
     */
    public static List<ClipboardTextConversionStrategy> getProcessors() {
        List<ClipboardTextConversionStrategy> conversionStrategies = Lists.newArrayList();
        conversionStrategies.add(new JsonConversionStrategy());
        conversionStrategies.add(new Json5ConversionStrategy());
        Set<DataFormatType> enabledFormats = STATE.getEnabledFormats();

        if (enabledFormats.contains(DataFormatType.XML)) {
            conversionStrategies.add(new XmlConversionStrategy());
        }

        if (enabledFormats.contains(DataFormatType.YAML)) {
            conversionStrategies.add(new YamlConversionStrategy());
        }

        if (enabledFormats.contains(DataFormatType.TOML)) {
            conversionStrategies.add(new TomlConversionStrategy());
        }

        if (enabledFormats.contains(DataFormatType.URL_PARAM)) {
            conversionStrategies.add(new UrlParamConversionStrategy());
        }

        return conversionStrategies;
    }

    public ClipboardTextConversionStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ClipboardTextConversionStrategy strategy) {
        this.strategy = strategy;
    }

}
