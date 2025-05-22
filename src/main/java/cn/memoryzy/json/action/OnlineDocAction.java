package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonGridComponentProvider;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
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
        // PlatformUtil.openOnlineDoc(event.getProject(), popupAction);

        String json = Messages.showInputDialog("", "", Messages.getInformationIcon());

        // String json = "[\n" +
        //         "1,2,3,4,{\"a\": 90}\n" +
        //         "]";

        JsonGridComponentProvider provider = new JsonGridComponentProvider(JsonUtil.parse(json));

        new DialogBuilder()
                .centerPanel(provider.getTableComponent())
                .show();

    }

}
