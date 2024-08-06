package cn.memoryzy.json.group;

import cn.memoryzy.json.actions.JsonBeautifyAction;
import cn.memoryzy.json.actions.JsonMinifyAction;
import cn.memoryzy.json.actions.JsonStructureAction;
import cn.memoryzy.json.actions.ShortcutAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.UpdateHolder;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.*;
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
public class JsonProcessingPopupGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    private final boolean actionEventPopup;

    public JsonProcessingPopupGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.processing.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.processing.description"));
        presentation.setIcon(JsonAssistantIcons.BOX);
        this.actionEventPopup = false;
    }

    public JsonProcessingPopupGroup(boolean actionEventPopup) {
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
        actions.add(new JsonBeautifyAction());
        actions.add(new JsonMinifyAction());
        actions.add(new JsonStructureAction());
        // ------- 分隔符
        actions.add(Separator.create());
        if (actionEventPopup || PlatformUtil.isNewUi()) {
            actions.add(Separator.create(JsonAssistantBundle.message("separator.transform")));
        }

        actions.add(new ConvertOtherFormatsGroup());
        actions.add(Separator.create());
        actions.add(new ShortcutAction());

        return actions.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(UpdateHolder.isHasJsonStr());
    }
}