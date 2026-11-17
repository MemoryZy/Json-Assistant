package cn.memoryzy.json.action.organize;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonValuePruner;
import icons.JsonAssistantIcons;

/**
 * 整理 Json：去除 0 值
 * <p>
 * 只处理「数值类型的 0」（{@code 0}、{@code 0.0}、{@code -0} 等等价写法）；
 * 布尔值 {@code false} 与字符串 {@code "0"} 都不算 0 值，会原样保留。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class RemoveZeroValueAction extends OrganizeJsonAction {

    public RemoveZeroValueAction() {
        super(JsonAssistantBundle.message("action.organize.json.remove.zero.text"),
                // 描述文案每个子项单独一条，方便在中英文之间留出正确的空格
                JsonAssistantBundle.messageOnSystem("action.organize.json.remove.zero.description"),
                JsonValuePruner.Mode.ZERO,
                // 类别名只用于提示文案，例如「已去除 4 项（0 值）」
                "category.organize.json.zero");
    }
}
