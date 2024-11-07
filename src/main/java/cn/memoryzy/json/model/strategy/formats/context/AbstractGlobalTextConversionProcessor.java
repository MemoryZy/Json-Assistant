package cn.memoryzy.json.model.strategy.formats.context;

import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.data.ActionData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.FileTypeData;
import cn.memoryzy.json.model.data.MessageData;
import cn.memoryzy.json.util.JsonUtil;

/**
 * @author Memory
 * @since 2024/10/31
 */
public abstract class AbstractGlobalTextConversionProcessor implements GlobalTextConversionProcessor {

    /**
     * 当前获取的文本（符合格式的文本）
     */
    protected String content;

    /**
     * 解析编辑器文本的成功与否状态
     */
    protected TextResolveStatus textResolveStatus;

    /**
     * 转换完成的 JSON 文本是否需要格式化
     */
    private final boolean needBeautify;

    /**
     * 处理器所代表的数据类型
     */
    protected final FileTypeData fileTypeData;

    /**
     * 编辑器相关信息
     */
    protected final EditorData editorData;

    /**
     * 当文本匹配成功，用于替换的操作信息
     */
    protected final ActionData actionData;

    /**
     * 文本转换后的提示信息
     */
    protected final MessageData messageData;


    protected AbstractGlobalTextConversionProcessor(EditorData editorData, boolean needBeautify) {
        this.editorData = editorData;
        this.needBeautify = needBeautify;
        this.actionData = createActionData();
        this.messageData = createMessageData();
        this.fileTypeData = createFileTypeData();
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
    protected ActionData createActionData() {
        return new ActionData();
    }

    /**
     * 构建文本转换后的提示信息相关信息
     */
    protected MessageData createMessageData() {
        return new MessageData();
    }

    /**
     * 构建文件类型所代表的类型，默认 JSON5 类型（JSON5类型亦支持普通JSON）
     */
    protected FileTypeData createFileTypeData() {
        return new FileTypeData().setProcessedFileType(FileTypeHolder.JSON5);
    }


    @Override
    public void preprocessing() {
    }

    @Override
    public String postprocessing(String text) throws Exception {
        return needBeautify ? JsonUtil.formatJson(text) : JsonUtil.compressJson(text);
    }

    // ----------------------- GETTER/SETTER -----------------------


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public TextResolveStatus getTextResolveStatus() {
        return textResolveStatus;
    }

    public void setTextResolveStatus(TextResolveStatus textResolveStatus) {
        this.textResolveStatus = textResolveStatus;
    }

    public boolean isNeedBeautify() {
        return needBeautify;
    }

    public FileTypeData getFileTypeData() {
        return fileTypeData;
    }

    public EditorData getEditorData() {
        return editorData;
    }

    public ActionData getActionData() {
        return actionData;
    }

    public MessageData getMessageData() {
        return messageData;
    }
}
