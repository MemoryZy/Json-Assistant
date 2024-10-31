package cn.memoryzy.json.model.strategy;

/**
 * @author Memory
 * @since 2024/10/31
 */
public interface ConversionStrategy {

    boolean canConvert(String text) throws Exception;

    String convertToJson(String text) throws Exception;

}
