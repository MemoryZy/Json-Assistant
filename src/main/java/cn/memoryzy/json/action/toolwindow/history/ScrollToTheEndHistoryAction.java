package cn.memoryzy.json.action.toolwindow.history;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/9/18
 */
public class ScrollToTheEndHistoryAction extends ToggleAction implements DumbAware, UpdateInBackground {

    private final HistoryToolWindowComponentProvider provider;

    public ScrollToTheEndHistoryAction(HistoryToolWindowComponentProvider provider) {
        super(JsonAssistantBundle.messageOnSystem("action.scroll.to.end.text"), JsonAssistantBundle.messageOnSystem("action.scroll.to.end.description"), JsonAssistantIcons.ToolWindow.SCROLL_DOWN);
        this.provider = provider;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        Editor editor = provider.getActiveEditor();
        Document document = Objects.requireNonNull(editor).getDocument();
        return document.getLineCount() == 0 || document.getLineNumber(editor.getCaretModel().getOffset()) == document.getLineCount() - 1;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean state) {
        Editor editor = provider.getActiveEditor();
        if (state) {
            EditorUtil.scrollToTheEnd(Objects.requireNonNull(editor));
        } else {
            int lastLine = Math.max(0, Objects.requireNonNull(editor).getDocument().getLineCount() - 1);
            LogicalPosition currentPosition = editor.getCaretModel().getLogicalPosition();
            LogicalPosition position = new LogicalPosition(Math.max(0, Math.min(currentPosition.line, lastLine - 1)), currentPosition.column);
            editor.getCaretModel().moveToLogicalPosition(position);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        Project project = getEventProject(e);
        Editor editor = provider.getActiveEditor();
        boolean enabled = project != null && editor != null;

        if (enabled) {
            boolean selected = isSelected(e);
            Toggleable.setSelected(presentation, selected);
        }

        presentation.setEnabled(enabled);
    }
}
