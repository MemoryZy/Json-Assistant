package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/20
 */
public class ScrollToTheEndAction extends ScrollToTheEndToolbarAction {

    private final Editor editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public ScrollToTheEndAction(@NotNull Editor editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super(editor);
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.scroll.to.end.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.scroll.to.end.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SCROLL_DOWN);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(
                getEventProject(e) != null
                        && StrUtil.isNotBlank(editor.getDocument().getText())
                        && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel));
    }

}
