package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class OnlineDocAction extends DumbAwareAction {

    public OnlineDocAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.online.doc.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.online.doc.description"));
        presentation.setIcon(JsonAssistantIcons.BOOK);
        addTextOverride(ActionPlaces.EDITOR_POPUP, JsonAssistantBundle.message("action.online.doc.override.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse(HyperLinks.OVERVIEW);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        if (Objects.equals(ActionPlaces.EDITOR_POPUP, e.getPlace())) {
            presentation.setIcon(AllIcons.Actions.Help);
        } else {
            presentation.setIcon(JsonAssistantIcons.BOOK);
        }
    }
}
