package cn.memoryzy.json.model.strategy;

/**
 * @author Memory
 * @since 2024/10/31
 */
public interface ConversionStrategy {

    boolean canConvert(String text);

    String convertToJson(String text);

}
