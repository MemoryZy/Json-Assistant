package cn.memoryzy.json.extension.editor;

import cn.memoryzy.json.ui.JsonEditorComponentProvider;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author Memory
 * @since 2024/12/16
 */
public class JsonFileEditor extends UserDataHolderBase implements FileEditor {

    private final Project project;
    private final VirtualFile file;
    private final JsonEditorComponentProvider provider;

    public JsonFileEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        // 第一次加载，不加载树
        this.provider = new JsonEditorComponentProvider(project, null);
    }

    @Override
    public @NotNull JComponent getComponent() {
        return provider.getComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return provider.getPreferredFocusedComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Structure";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return FileEditor.super.getState(level);
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getFile() {
        return file;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void selectNotify() {
        provider.selectNotify(PlatformUtil.getFileRealContent(project, file));
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
