package cn.memoryzy.json.action.organize;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonValuePruner;
import icons.JsonAssistantIcons;

/**
 * 整理 Json：全部去除（一次完成 null 值、空值、0 值的清理）
 * <p>
 * 等价于按 {@link JsonValuePruner.Mode#ALL} 模式清理：
 * 三类数据一并去除，并且由于包含「空值」，递归清理后变空的对象/数组也会被级联移除。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class RemoveAllValuesAction extends OrganizeJsonAction {

    public RemoveAllValuesAction() {
        super(JsonAssistantBundle.message("action.organize.json.remove.all.text"),
                // 描述文案每个子项单独一条，方便在中英文之间留出正确的空格
                JsonAssistantBundle.messageOnSystem("action.organize.json.remove.all.description"),
                JsonValuePruner.Mode.ALL,
                // 类别名只用于提示文案，例如「已去除 9 项（null 值、空值与 0 值）」
                "category.organize.json.all");
    }
}
