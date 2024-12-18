package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Memory
 * @since 2024/12/13
 */
public class BackToEditorViewAction extends DumbAwareAction implements UpdateInBackground {

    private final ToolWindowEx toolWindow;

    public BackToEditorViewAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.toggle.editor.card.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.toggle.editor.card.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.EYE_OFF);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
        Optional.ofNullable(ToolWindowUtil.getPanelOnContent(selectedContent))
                .ifPresent(panel -> panel.switchToCard(null, PluginConstant.JSON_EDITOR_CARD_NAME));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有当前处于树视图才能切换
        Content selectedContent = ToolWindowUtil.getSelectedContent(toolWindow);
        Boolean treeCardDisplayed = Optional.ofNullable(ToolWindowUtil.getPanelOnContent(selectedContent))
                .map(JsonAssistantToolWindowPanel::getCardLayout)
                .map(el -> el.isTreeCardDisplayed() || el.isPathCardDisplayed())
                .orElse(false);

        e.getPresentation().setEnabledAndVisible(treeCardDisplayed);
    }
}
