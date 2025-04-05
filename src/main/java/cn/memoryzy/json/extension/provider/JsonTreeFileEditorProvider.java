package cn.memoryzy.json.extension.provider;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.extension.editor.JsonFileEditor;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonTreeFileEditorProvider implements FileEditorProvider {

    // 切换标签页
    // FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    // fileEditorManager.setSelectedEditor(openFile, JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonTreeFileEditor");

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return PlatformUtil.isJsonFileType(file.getFileType());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new JsonFileEditor(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonTreeFileEditor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }

}
