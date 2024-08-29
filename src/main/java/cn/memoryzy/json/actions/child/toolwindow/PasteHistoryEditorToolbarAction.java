package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class PasteHistoryEditorToolbarAction extends DumbAwareAction implements UpdateInBackground {

    public PasteHistoryEditorToolbarAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.FORM_HISTORY);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        if (project == null || editor == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            LimitedList<String> history = JsonViewerHistoryState.getInstance(project).getHistory();
            if (CollUtil.isNotEmpty(history)) {
                editor.getDocument().setText(history.get(0));
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) return;

        LimitedList<String> history = JsonViewerHistoryState.getInstance(project).getHistory();
        if (CollUtil.isEmpty(history)) return;

        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(project);
        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);

        Editor editor = PlatformUtil.getEditor(e);
        if (editor == null || selectedContent == null) return;


        // todo 这里如果没满足，顺便hide掉toolbar


    }
}
