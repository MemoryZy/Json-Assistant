package cn.memoryzy.json.model.strategy.formats;

import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.MessageInfo;
import cn.memoryzy.json.util.JsonUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public abstract class AbstractConversionProcessor implements ConversionProcessor {

    /**
     * 当前获取的文本（符合格式的文本）
     */
    protected String content;

    /**
     * 处理器所代表的数据类型（类全限定名）
     */
    protected final String fileTypeClassName;

    /**
     * 编辑器相关信息
     */
    protected final EditorInfo editorInfo;

    /**
     * 当文本匹配成功，用于替换的操作信息
     */
    protected final ActionInfo actionInfo;

    /**
     * 文本转换后的提示信息
     */
    protected final MessageInfo messageInfo;


    protected AbstractConversionProcessor(EditorInfo editorInfo, String fileTypeClassName) {
        this.fileTypeClassName = fileTypeClassName;
        this.editorInfo = editorInfo;
        this.actionInfo = createActionInfo();
        this.messageInfo = createMessageInfo();
    }


    public final String convert(String text) {
        try {
            if (canConvert(text)) {
                // 设置内容
                setContent(text);
                // 执行前置逻辑
                preprocessing();
                // 执行转换逻辑
                String json = convertToJson();
                // 执行后置逻辑
                return postprocessing(json);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }


    /**
     * 构建操作相关信息
     */
    protected abstract ActionInfo createActionInfo();

    /**
     * 构建文本转换后的提示信息相关信息
     */
    protected abstract MessageInfo createMessageInfo();


    @Override
    public void preprocessing() throws Exception {
    }

    @Override
    public String postprocessing(String text) throws Exception {
        return JsonUtil.formatJson(text);
    }

    // ----------------------- GETTER/SETTER -----------------------


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getFileTypeClassName() {
        return fileTypeClassName;
    }

    public EditorInfo getEditorInfo() {
        return editorInfo;
    }

    public ActionInfo getActionInfo() {
        return actionInfo;
    }

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }
}
