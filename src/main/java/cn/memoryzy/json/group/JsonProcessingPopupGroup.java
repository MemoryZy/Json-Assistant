package cn.memoryzy.json.group;

import cn.memoryzy.json.actions.JsonBeautifyAction;
import cn.memoryzy.json.actions.JsonMinifyAction;
import cn.memoryzy.json.actions.JsonStructureAction;
import cn.memoryzy.json.actions.ShortcutAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/2
 */
public class JsonProcessingPopupGroup extends DefaultActionGroup implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        DataContext dataContext = e.getDataContext();
        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(JsonAssistantBundle.messageOnSystem("popup.json.processing.title"),
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
                new ShortcutAction()
                // Separator.create("Transform"),


        };
    }
}