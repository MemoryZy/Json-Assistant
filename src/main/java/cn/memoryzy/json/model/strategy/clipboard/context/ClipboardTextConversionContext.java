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

        if (enabledFormats.contains(DataFormatType.TYPE_SCRIPT)) {
            conversionStrategies.add(new TypeScriptConversionStrategy());
        }

        if (enabledFormats.contains(DataFormatType.URL_PARAM)) {
            conversionStrategies.add(new UrlParamConversionStrategy());
        }

        if (enabledFormats.contains(DataFormatType.BULK_KV_LINE)) {
            // Bulk KV Lines（批量键值行格式）：始终启用（与 JSON、JSON5 一致，不提供开关）。
            // 必须放在最后：key: value 这类带空格的写法同时也是合法 YAML，
            // 排在其他格式之后，保证「能按其他格式解析的内容优先由其他格式处理」，
            // 只有当其他格式都不成立（例如 key:value 冒号后无空格、或含 list[0] 下标）时才按 Bulk KV Lines 处理。
            conversionStrategies.add(new BulkKvLinesConversionStrategy());
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
