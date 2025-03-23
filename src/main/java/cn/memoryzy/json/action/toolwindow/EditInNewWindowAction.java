package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/27
 */
public class EditInNewWindowAction extends DumbAwareAction implements UpdateInBackground {
    private final ToolWindow toolWindow;

    public EditInNewWindowAction(ToolWindow toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.edit.new.window.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.edit.new.window.description"));
        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl alt M"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        final FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile virtualFile = getVirtualFile(project);
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        rename(project, virtualFile, content);
        JsonAssistantUtil.invokeMethod(manager, "openFileInNewWindow", virtualFile);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(getEventProject(event) != null);
    }

    @SuppressWarnings("DataFlowIssue")
    public VirtualFile getVirtualFile(Project project) {
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        EditorEx editor = Optional.ofNullable(content).map(ToolWindowUtil::getEditorOnContent).orElse(null);
        String text = editor == null ? "" : editor.getDocument().getText();
        return Optional.ofNullable(editor)
                .map(Editor::getDocument)
                .map(document -> PsiDocumentManager.getInstance(project).getPsiFile(document))
                .map(PsiFile::getVirtualFile)
                .orElse(new LightVirtualFile(content.getDisplayName(), FileTypeHolder.JSON5, text));
    }


    public static void rename(Project project, VirtualFile virtualFile, Content content) {
        String displayName = content.getDisplayName();
        String name = virtualFile.getNameWithoutExtension();
        if (Objects.equals(displayName, name)) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                virtualFile.rename(virtualFile, displayName);
            } catch (IOException ignored) {
            }
        });
    }
}
