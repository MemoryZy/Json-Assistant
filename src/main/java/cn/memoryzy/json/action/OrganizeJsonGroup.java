package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 「整理 Json…」分组
 * <p>
 * 写法与「Json 排序…」（{@link SortGroup}）保持一致：
 * <ul>
 *     <li>分组本身不执行任何处理，只负责展示子菜单</li>
 *     <li>菜单里同时存在「注册实例」（plugin.xml 中注册、由 MainGroup 取用）与「弹窗实例」
 *     （{@code new OrganizeJsonGroup(true)}），后者用于明确表示「当前在主弹窗中展示」</li>
 *     <li>只有当「在主弹窗中展示」或者「处于新 UI」时，才显示带文字的分隔线，
 *     避免在旧 UI 的工具按钮下拉中多出一行无意义的分组标题</li>
 * </ul>
 * 子菜单结构：
 * <ol>
 *     <li>去除 null 值</li>
 *     <li>去除空值</li>
 *     <li>去除 0 值</li>
 *     <li>全部去除（分隔线下方，一次完成上面三项）</li>
 * </ol>
 * 之所以把「全部去除」放在最后并用分隔线隔开，是为了让它在视觉上与三个单项区分开，
 * 避免用户误以为它只是又一个单项。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class OrganizeJsonGroup extends DumbAwareBaseActionGroup {

    /**
     * 是否「在主弹窗中展示」
     * <p>
     * MainGroup 用 {@code new OrganizeJsonGroup(true)} 创建弹窗专用的实例；
     * plugin.xml 中注册的实例则该字段为 false。
     */
    private final boolean fromPopup;

    public OrganizeJsonGroup() {
        this(false);
    }

    public OrganizeJsonGroup(boolean fromPopup) {
        super(JsonAssistantBundle.message("group.organize.json.text"),
                JsonAssistantBundle.messageOnSystem("group.organize.json.description"),
                JsonAssistantIcons.TIDY);
        // 以弹出子菜单的形式展示（与「Json 排序…」「扩展功能…」等分组保持一致）
        setPopup(true);
        setEnabledInModalContext(true);
        this.fromPopup = fromPopup;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        // 与 SortGroup 一致：主弹窗展示或新 UI 时，才显示带文字的分隔线
        // boolean showSeparatorText = fromPopup || PlatformUtil.isNewUi();

        List<AnAction> actions = new ArrayList<>();
        // if (showSeparatorText) {
        //     actions.add(Separator.create(JsonAssistantBundle.message("separator.organize.json")));
        // }

        // 三个单项按「null → 空值 → 0 值」的顺序排列，与菜单中常见的阅读顺序一致
        actions.add(ActionHolder.Organize.REMOVE_EMPTY_VALUE_ACTION);
        actions.add(ActionHolder.Organize.REMOVE_ZERO_VALUE_ACTION);
        actions.add(ActionHolder.Organize.REMOVE_NULL_VALUE_ACTION);

        // 分隔线：下面是「全部去除」，一次性完成上面三项
        actions.add(Separator.create());
        actions.add(ActionHolder.Organize.REMOVE_ALL_VALUES_ACTION);

        return actions.toArray(new AnAction[0]);
    }
}
