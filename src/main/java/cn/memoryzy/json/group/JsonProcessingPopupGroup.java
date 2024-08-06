package cn.memoryzy.json.group;

import cn.memoryzy.json.actions.*;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.UpdateHolder;
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

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonProcessingPopupGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    public JsonProcessingPopupGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.json.processing.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.processing.description"));
        presentation.setIcon(PlatformUtil.isNewUi() ? JsonAssistantIcons.ExpUi.NEW_BOX : JsonAssistantIcons.BOX);
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
        return new AnAction[]{
                new JsonBeautifyAction(),
                new JsonMinifyAction(),
                new JsonStructureAction(),
                Separator.create(),
                Separator.create(JsonAssistantBundle.message("separator.transform")),
                new ConvertOtherFormatsGroup(),
                Separator.create(),
                new ShortcutAction()
        };
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(UpdateHolder.isHasJsonStr());
    }
}