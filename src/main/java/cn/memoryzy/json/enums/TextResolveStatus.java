package cn.memoryzy.json.enums;

/**
 * 解析编辑器文本的成功与否状态
 *
 * @author Memory
 * @since 2024/11/3
 */
public enum TextResolveStatus {

    /**
     * 选中了特定文本，并且解析成功
     */
    SELECTED_SUCCESS,

    /**
     * 没有选中文本，解析默认的全部文本成功
     */
    GLOBAL_SUCCESS,

    /**
     * 解析失败
     */
    RESOLVE_FAILED

}
