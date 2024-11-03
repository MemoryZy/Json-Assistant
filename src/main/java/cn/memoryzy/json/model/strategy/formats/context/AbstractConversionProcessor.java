package cn.memoryzy.json.model.strategy.formats.context;

import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.TextResolveStatus;
import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.FileTypeInfo;
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
     * 解析编辑器文本的成功与否状态
     */
    protected TextResolveStatus textResolveStatus;

    /**
     * 转换完成的 JSON 文本是否需要格式化
     */
    private final boolean needsFormatting;

    /**
     * 处理器所代表的数据类型
     */
    protected final FileTypeInfo fileTypeInfo;

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


    protected AbstractConversionProcessor(EditorInfo editorInfo, boolean needsFormatting) {
        this.editorInfo = editorInfo;
        this.needsFormatting = needsFormatting;
        this.actionInfo = createActionInfo();
        this.messageInfo = createMessageInfo();
        this.fileTypeInfo = buildFileTypeInfo();
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

    /**
     * 构建支持写入的文件类型，类全限定名（通常只是 JSON 类型赋此值，其他类型可置为 null）
     */
    protected String[] createAllowedFileTypeQualifiedNames() {
        // 默认为 null
        return null;
    }

    /**
     * 构建文件类型所代表的类型，默认 JSON 类型
     */
    private FileTypeInfo buildFileTypeInfo() {
        return new FileTypeInfo().setProcessedFileType(FileTypeHolder.JSON).setAllowedFileTypeQualifiedNames(createAllowedFileTypeQualifiedNames());
    }


    @Override
    public void preprocessing() {
    }

    @Override
    public String postprocessing(String text) {
        return needsFormatting ? JsonUtil.formatJson(text) : text;
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

    public boolean isNeedsFormatting() {
        return needsFormatting;
    }

    public FileTypeInfo getFileTypeInfo() {
        return fileTypeInfo;
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
