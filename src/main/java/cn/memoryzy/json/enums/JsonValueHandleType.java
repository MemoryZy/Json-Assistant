package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

/**
 * @author Memory
 * @since 2025/2/26
 */
public enum JsonValueHandleType {

    NESTED_JSON("hint.selection.ExpandAllNestedJson", "hint.global.ExpandAllNestedJson"),

    TIMESTAMP("hint.selection.convertAllTimestamp", "hint.global.convertAllTimestamp");

    private final String selectionConvertSuccessMessageKey;
    private final String globalConvertSuccessMessageKey;

    JsonValueHandleType(String selectionConvertSuccessMessageKey, String globalConvertSuccessMessageKey) {
        this.selectionConvertSuccessMessageKey = selectionConvertSuccessMessageKey;
        this.globalConvertSuccessMessageKey = globalConvertSuccessMessageKey;
    }

    public String getSelectionConvertSuccessMessage() {
        return JsonAssistantBundle.messageOnSystem(selectionConvertSuccessMessageKey);
    }

    public String getGlobalConvertSuccessMessage() {
        return JsonAssistantBundle.messageOnSystem(globalConvertSuccessMessageKey);
    }
}
