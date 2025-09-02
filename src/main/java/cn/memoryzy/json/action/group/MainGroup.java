package cn.memoryzy.json.action.group;

import cn.memoryzy.json.action.OnlineDocAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
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
public class MainGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    private final boolean fromPopup;

    @SuppressWarnings("unused")
    public MainGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.main.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.main.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
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

        if (showSeparatorText) {
            actions.add(new SortGroup(true));
        } else {
            actions.add(ActionHolder.Sort.SORT_GROUP);
        }

        actions.add(Separator.create());
        actions.add(ActionHolder.Main.JSON_TEXT_DIFF_ACTION);
        // ------- 分隔符
        actions.add(Separator.create());
        if (showSeparatorText) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.transform")));
        }

        actions.add(ActionHolder.Main.JSON_ESCAPE_ACTION);
        actions.add(Separator.create());
        actions.add(ActionHolder.Convert.CONVERT_OTHER_FORMATS_GROUP);
        // ------- 分隔符
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