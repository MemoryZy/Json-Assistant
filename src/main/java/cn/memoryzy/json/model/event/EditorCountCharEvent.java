package cn.memoryzy.json.model.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Memory
 * @since 2025/8/26
 */
public class EditorCountCharEvent {

    private final Project project;
    private final Editor editor;
    private final VirtualFile file;
    private final int totalChars;

    public EditorCountCharEvent(Project project, Editor editor, VirtualFile file, int totalChars) {
        this.project = project;
        this.editor = editor;
        this.file = file;
        this.totalChars = totalChars;
    }

    public Project getProject() {
        return project;
    }

    public Editor getEditor() {
        return editor;
    }

    public VirtualFile getFile() {
        return file;
    }

    public int getTotalChars() {
        return totalChars;
    }
}
