package cn.memoryzy.json.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/8/25
 */
// @Service(Service.Level.PROJECT)
public final class ProjectEditorManager implements Disposable {

    private final Project project;
    private final List<Editor> editors = ContainerUtil.createLockFreeCopyOnWriteList();

    public ProjectEditorManager(@NotNull Project project) {
        this.project = project;
    }

    public static ProjectEditorManager getInstance(@NotNull Project project) {
        return project.getService(ProjectEditorManager.class);
    }

    public void addEditor(@NotNull Editor editor) {
        editors.add(editor);
    }

    public void removeEditor(@NotNull Editor editor) {
        editors.remove(editor);
    }

    @NotNull
    public List<Editor> getEditors() {
        return List.copyOf(editors);
    }

    @Override
    public void dispose() {
        // 服务被释放时自动清除所有编辑器
        editors.forEach(editor -> {
            if (!editor.isDisposed()) {
                EditorFactory.getInstance().releaseEditor(editor);
            }
        });
        editors.clear();
    }
}
