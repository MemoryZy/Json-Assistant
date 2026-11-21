package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.JsonExtractUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class MainGroup extends DumbAwareBaseActionGroup {

    private final boolean fromPopup;

    @SuppressWarnings("unused")
    public MainGroup() {
        super(JsonAssistantBundle.message("action.main.text"), JsonAssistantBundle.messageOnSystem("action.main.description"), JsonAssistantIcons.BOX);
        setPopup(true);
        setEnabledInModalContext(true);
        this.fromPopup = false;
    }

    public MainGroup(boolean fromPopup) {
        this.fromPopup = fromPopup;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        showPopupMenu(event.getDataContext());
    }

    public void showPopupMenu(DataContext dataContext) {
        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(JsonAssistantBundle.message("popup.menu.title"),
                        this, dataContext, JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true);
        popup.showInBestPositionFor(dataContext);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        boolean showSeparatorText = fromPopup || PlatformUtil.isNewUi();

        List<AnAction> actions = new ArrayList<>();
        actions.add(ActionHolder.Main.JSON_BEAUTIFY_ACTION);
        actions.add(ActionHolder.Main.JSON_MINIFY_ACTION);
        actions.add(ActionHolder.Main.JSON_STRUCTURE_ACTION);
        actions.add(Separator.create());
        actions.add(ActionHolder.Main.JSON_GRID_ACTION);
        actions.add(Separator.create());

        // 整理 Json…（子菜单为去除 null 值 / 空值 / 0 值，以及全部去除）
        // 与排序分组一致：弹窗（或新 UI）时用弹窗实例，以便显示带文字的分隔线；否则用注册实例
        if (showSeparatorText) {
            actions.add(new OrganizeJsonGroup(true));
        } else {
            actions.add(ActionHolder.Organize.ORGANIZE_JSON_GROUP);
        }
        // 提取…（复制光标处的 Json 键 / 值 / 键值对 / 路径）
        // 仅在以 Json 格式打开的编辑器中加入菜单，其余编辑器不展示（避免留下多余的分隔线）
        if (event != null && ReadAction.compute(() -> JsonExtractUtil.isJsonEditor(event.getDataContext()))) {
            actions.add(Separator.create());
            actions.add(ActionHolder.Extract.EXTRACT_GROUP);
        }

        // 排序：跟在「整理」与「提取」之后
        actions.add(Separator.create());
        if (showSeparatorText) {
            actions.add(new SortGroup(true));
        } else {
            actions.add(ActionHolder.Sort.SORT_GROUP);
        }

        // ------- 转换（转换组内部再以分割线隔出 Json 转义）
        actions.add(Separator.create());
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.transform")));
        }

        actions.add(ActionHolder.Convert.CONVERT_OTHER_FORMATS_GROUP);

        // Json 文本对比：紧跟在转换组下方
        actions.add(Separator.create());
        actions.add(ActionHolder.Main.JSON_TEXT_DIFF_ACTION);

        // ------- 扩展
        actions.add(Separator.create());
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.extend")));
        }

        actions.add(ActionHolder.Extend.EXTEND_GROUP);
        actions.add(Separator.create());
        actions.add(ActionHolder.Main.SHORTCUT_ACTION);
        actions.add(Separator.create());
        actions.add(new OnlineDocAction(JsonAssistantBundle.message("action.online.doc.override.text"), AllIcons.Actions.Help));

        return actions.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabledAndVisible(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), PlatformUtil.getEditor(dataContext)));
    }

}