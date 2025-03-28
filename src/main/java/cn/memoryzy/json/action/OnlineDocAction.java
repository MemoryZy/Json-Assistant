package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class OnlineDocAction extends DumbAwareAction implements UpdateInBackground {
    private final boolean popupAction;

    public OnlineDocAction() {
        this(false);
    }

    public OnlineDocAction(boolean popupAction) {
        super();
        this.popupAction = popupAction;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(popupAction ? JsonAssistantBundle.message("action.online.doc.override.text") : JsonAssistantBundle.message("action.online.doc.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.online.doc.description"));
        presentation.setIcon(popupAction ? AllIcons.Actions.Help : JsonAssistantIcons.BOOK_READER);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PlatformUtil.openOnlineDoc(event.getProject(), popupAction);
    }

}
