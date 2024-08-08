package cn.memoryzy.json.group;

import cn.memoryzy.json.actions.JsonAssistantAction;
import cn.memoryzy.json.actions.OnlineDocAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonAssistantPopupGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    private final boolean actionEventPopup;

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
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        DataContext dataContext = e.getDataContext();
        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(JsonAssistantBundle.message("popup.json.processing.title"),
                        this, dataContext, JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true);
        popup.showInBestPositionFor(dataContext);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(ActionHolder.JSON_BEAUTIFY_ACTION);
        actions.add(ActionHolder.JSON_MINIFY_ACTION);
        actions.add(ActionHolder.JSON_STRUCTURE_ACTION);
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
    public void update(@NotNull AnActionEvent e) {
        Editor editor = PlatformUtil.getEditor(e);
        e.getPresentation().setEnabledAndVisible(editor != null && JsonAssistantAction.isOrHasJsonStr(editor));
    }
}