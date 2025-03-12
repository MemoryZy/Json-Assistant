package cn.memoryzy.json.extension.provider;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.extension.editor.JsonFileEditor;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonFileEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return PlatformUtil.isJsonFileType(file.getFileType()) && StrUtil.isNotBlank(PlatformUtil.getFileContent(file));
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new JsonFileEditor(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return JsonAssistantPlugin.PLUGIN_ID_NAME + ".FileEditor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }

    @Override
    public @NotNull FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return (otherState, level) -> false;
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        // System.out.println("called writeState()");
    }
}
