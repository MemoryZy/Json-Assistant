package cn.memoryzy.json.extension.provider;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.extension.editor.JsonFileEditor;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonFileEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOG = Logger.getInstance(JsonFileEditorProvider.class);

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return PlatformUtil.isJsonFileType(file.getFileType()) && StrUtil.isNotBlank(getContent(file));
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new JsonFileEditor(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return JsonAssistantPlugin.PLUGIN_ID_NAME + "FileEditor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }

    private String getContent(VirtualFile file) {
        String content = null;
        try {
            content = StrUtil.str(file.contentsToByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to get text", e);
        }

        return content;
    }

}
