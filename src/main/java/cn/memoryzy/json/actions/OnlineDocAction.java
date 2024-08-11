package cn.memoryzy.json.actions;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class OnlineDocAction extends DumbAwareAction {

    public OnlineDocAction() {
        this(false);
    }

    public OnlineDocAction(boolean popupAction) {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(popupAction ? JsonAssistantBundle.message("action.online.doc.override.text") : JsonAssistantBundle.message("action.online.doc.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.online.doc.description"));
        presentation.setIcon(popupAction ? AllIcons.Actions.Help : JsonAssistantIcons.BOOK);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // BrowserUtil.browse(HyperLinks.OVERVIEW);
        JsonViewerHistoryState state = JsonViewerHistoryState.getInstance(e.getProject());
        List<String> historyList = state.getHistoryList();
        if (CollUtil.isNotEmpty(historyList)) {
            String record = historyList.get(historyList.size() - 1);
            System.out.println(record);
            return;
        }

        System.out.println("empty");
    }

}
