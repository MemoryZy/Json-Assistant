package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction {

    public JsonHistoryAction() {
        super(JsonAssistantBundle.messageOnSystem("action.json.history.text"),
                JsonAssistantBundle.messageOnSystem("action.json.history.description"),
                JsonAssistantIcons.HISTORY);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        JsonViewerHistoryState historyState = JsonViewerHistoryState.getInstance(project);
        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 2), source.getHeight() + 1));

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(new ArrayList<>())
                .setMovable(true)
                .createPopup()
                .show(relativePoint);

        // SearchTextField
    }

}
