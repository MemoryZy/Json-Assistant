package cn.memoryzy.json.model.strategy.clipboard.context;

/**
 * 剪贴板的文本转为 JSON 的处理
 *
 * @author Memory
 * @since 2024/10/31
 */
public interface ClipboardTextConversionStrategy {

    String type();

    boolean canConvert(String text) throws Exception;

    String convertToJson(String text) throws Exception;

}
