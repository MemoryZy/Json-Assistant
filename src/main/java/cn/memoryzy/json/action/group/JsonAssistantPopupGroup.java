package cn.memoryzy.json.action.group;

import cn.memoryzy.json.action.OnlineDocAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
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
public class JsonAssistantPopupGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    private final boolean actionEventPopup;

    @SuppressWarnings("unused")
    public JsonAssistantPopupGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.assistant.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.assistant.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
        this.actionEventPopup = false;
    }

    public JsonAssistantPopupGroup(boolean actionEventPopup) {
        this.actionEventPopup = actionEventPopup;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        showPopupMenu(event.getDataContext());
    }

    public void showPopupMenu(DataContext dataContext) {
        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(JsonAssistantBundle.message("menu.popup.title"),
                        this, dataContext, JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true);
        popup.showInBestPositionFor(dataContext);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(ActionHolder.JSON_BEAUTIFY_ACTION);
        actions.add(ActionHolder.JSON_MINIFY_ACTION);
        actions.add(ActionHolder.JSON_STRUCTURE_ACTION);
        actions.add(Separator.create());
        actions.add(ActionHolder.JSON_TEXT_DIFF_ACTION);
        // ------- 分隔符
        actions.add(Separator.create());
        if (actionEventPopup || PlatformUtil.isNewUi()) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.transform")));
        }

        actions.add(ActionHolder.CONVERT_OTHER_FORMATS_GROUP);
        actions.add(Separator.create());
        actions.add(ActionHolder.SHORTCUT_ACTION);
        actions.add(Separator.create());
        actions.add(new OnlineDocAction(true));

        return actions.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabledAndVisible(
                GlobalJsonConverter.validateEditorJson(
                        getEventProject(event),
                        PlatformUtil.getEditor(dataContext),
                        dataContext));
    }

}