package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class MoveToEditorAction extends DumbAwareAction {
    private final ToolWindowEx toolWindow;

    public MoveToEditorAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.move.to.editor.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.move.to.editor.description"));
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt M"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);
        if (Objects.nonNull(selectedContent)) {
            EditorEx editor = JsonAssistantUtil.getEditorOnContent(selectedContent);
            String text = Objects.nonNull(editor) ? editor.getDocument().getText() : "";

            VirtualFile virtualFile = Optional.ofNullable(editor)
                    .map(Editor::getDocument)
                    .map(document -> PsiDocumentManager.getInstance(project).getPsiFile(document))
                    .map(PsiFile::getVirtualFile)
                    .orElse(new LightVirtualFile(selectedContent.getDisplayName(), JsonFileType.INSTANCE, text));

            EditInNewWindowAction.rename(project, virtualFile, selectedContent);
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
            toolWindow.hide();
        }
    }

}
