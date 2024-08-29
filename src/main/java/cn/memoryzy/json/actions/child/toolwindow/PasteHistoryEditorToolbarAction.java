package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.models.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class PasteHistoryEditorToolbarAction extends DumbAwareAction {

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

}
