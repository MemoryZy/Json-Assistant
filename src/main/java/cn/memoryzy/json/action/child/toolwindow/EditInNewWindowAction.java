package cn.memoryzy.json.action.child.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/27
 */
public class EditInNewWindowAction extends DumbAwareAction {
    private final ToolWindowEx toolWindow;

    public EditInNewWindowAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.edit.in.new.window.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.edit.in.new.window.description"));
        registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl alt M"), toolWindow.getComponent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile virtualFile = getVirtualFile(e);
        Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
        rename(project, virtualFile, content);
        ((FileEditorManagerImpl) manager).openFileInNewWindow(virtualFile);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(getEventProject(e) != null);
    }

    @SuppressWarnings("DataFlowIssue")
    public VirtualFile getVirtualFile(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
        LanguageTextField languageTextField = Optional.ofNullable(content).map(JsonAssistantUtil::getLanguageTextFieldOnContent).orElse(null);
        String text = languageTextField == null ? "" : languageTextField.getText();
        return Optional.ofNullable(languageTextField)
                .map(LanguageTextField::getEditor)
                .map(Editor::getDocument)
                .map(document -> PsiDocumentManager.getInstance(project).getPsiFile(document))
                .map(PsiFile::getVirtualFile)
                .orElse(new LightVirtualFile(content.getDisplayName(), JsonFileType.INSTANCE, text));
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