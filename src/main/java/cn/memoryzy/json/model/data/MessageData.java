package cn.memoryzy.json.model.data;

/**
 * 文本转换后的提示信息
 *
 * @author Memory
 * @since 2024/11/2
 */
public class MessageData {

    /**
     * 选中内容转换成功后的提示信息
     */
    private String selectionConvertSuccessMessage;

    /**
     * 全局内容转换成功后的提示信息
     */
    private String globalConvertSuccessMessage;




    // ----------------------- GETTER/SETTER -----------------------

    public String getSelectionConvertSuccessMessage() {
        return selectionConvertSuccessMessage;
    }

    public MessageData setSelectionConvertSuccessMessage(String selectionConvertSuccessMessage) {
        this.selectionConvertSuccessMessage = selectionConvertSuccessMessage;
        return this;
    }

    public String getGlobalConvertSuccessMessage() {
        return globalConvertSuccessMessage;
    }

    public MessageData setGlobalConvertSuccessMessage(String globalConvertSuccessMessage) {
        this.globalConvertSuccessMessage = globalConvertSuccessMessage;
        return this;
    }
}
