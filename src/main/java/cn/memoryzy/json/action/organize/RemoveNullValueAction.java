package cn.memoryzy.json.action.organize;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonValuePruner;
import icons.JsonAssistantIcons;

/**
 * 整理 Json：去除 null 值
 * <p>
 * 只处理 {@code null}，不处理空值（空字符串、空对象、空数组）与 0 值；
 * 因去除 null 而变空的对象/数组不会被级联移除（级联移除属于「空值」的范畴）。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class RemoveNullValueAction extends OrganizeJsonAction {

    public RemoveNullValueAction() {
        super(JsonAssistantBundle.message("action.organize.json.remove.null.text"),
                // 描述文案每个子项单独一条，方便在中英文之间留出正确的空格
                JsonAssistantBundle.messageOnSystem("action.organize.json.remove.null.description"),
                JsonValuePruner.Mode.NULL,
                // 类别名只用于提示文案，例如「已去除 5 项（null 值）」
                "category.organize.json.null");
    }
}
