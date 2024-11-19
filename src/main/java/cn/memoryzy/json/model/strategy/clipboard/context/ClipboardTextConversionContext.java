package cn.memoryzy.json.model.strategy.clipboard.context;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.strategy.clipboard.*;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.EditorBehaviorState;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 维护成功的策略
 *
 * @author Memory
 * @since 2024/10/31
 */
public class ClipboardTextConversionContext {
    private static final EditorBehaviorState STATE = JsonAssistantPersistentState.getInstance().editorBehaviorState;

    private ClipboardTextConversionStrategy strategy;

    public ClipboardTextConversionStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ClipboardTextConversionStrategy strategy) {
        this.strategy = strategy;
    }


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

        if (STATE.recognizeXmlFormat) {
            conversionStrategies.add(new XmlConversionStrategy());
        }

        if (STATE.recognizeYamlFormat) {
            conversionStrategies.add(new YamlConversionStrategy());
        }

        if (STATE.recognizeTomlFormat) {
            conversionStrategies.add(new TomlConversionStrategy());
        }

        if (STATE.recognizeUrlParamFormat) {
            conversionStrategies.add(new UrlParamConversionStrategy());
        }

        return conversionStrategies;
    }

}
