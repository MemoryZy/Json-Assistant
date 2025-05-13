package cn.memoryzy.json.model.strategy.formats.context;

/**
 * 全局的文本转为 JSON 的处理
 *
 * @author Memory
 * @since 2024/11/2
 */
public interface GlobalTextConversionProcessor {

    /**
     * 提供的文本是否符合转换为 JSON 格式的要求
     *
     * @param text 文本
     * @return 符合为 true；反之为 false
     * @throws Exception 异常
     */
    boolean canConvert(String text) throws Exception;

    /**
     * 转换为 JSON 格式
     *
     * @return JSON 文本
     * @throws Exception 异常
     */
    String convertToJson() throws Exception;

    /**
     * 执行转换方法前所执行的操作
     *
     * @throws Exception 异常
     */
    void preprocessing() throws Exception;

    /**
     * 执行转换方法后所执行的操作
     *
     * @throws Exception 异常
     */
    String postprocessing(String text) throws Exception;

}
