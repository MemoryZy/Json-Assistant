package cn.memoryzy.json.action.organize;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonValuePruner;
import icons.JsonAssistantIcons;

/**
 * 整理 Json：去除空值
 * <p>
 * 空值包含三类：空字符串（含纯空白串）、空对象 {@code {}}、空数组 {@code []}；
 * 嵌套的对象/数组会先递归清理，若清理后变成空对象/空数组，则它所在的位置也会被级联移除。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class RemoveEmptyValueAction extends OrganizeJsonAction {

    public RemoveEmptyValueAction() {
        super(JsonAssistantBundle.message("action.organize.json.remove.empty.text"),
                // 描述文案每个子项单独一条，方便在中英文之间留出正确的空格
                JsonAssistantBundle.messageOnSystem("action.organize.json.remove.empty.description"),
                JsonValuePruner.Mode.EMPTY,
                // 类别名只用于提示文案，例如「已去除 5 项（空值）」
                "category.organize.json.empty");
    }
}
