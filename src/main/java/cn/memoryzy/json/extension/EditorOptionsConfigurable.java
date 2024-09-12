package cn.memoryzy.json.extension;

import cn.memoryzy.json.service.EditorOptionsPersistentState;
import cn.memoryzy.json.ui.EditorOptionsComponentProvider;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class EditorOptionsConfigurable implements Configurable {

    private EditorOptionsPersistentState persistentState;
    private EditorOptionsComponentProvider componentProvider;

    @Override
    public String getDisplayName() {
        return "Json Viewer";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (persistentState == null) persistentState = EditorOptionsPersistentState.getInstance();
        if (componentProvider == null) componentProvider = new EditorOptionsComponentProvider(persistentState);
        return componentProvider.createRootPanel();
    }

    @Override
    public void reset() {
        if (componentProvider != null) componentProvider.reset();
    }

    @Override
    public boolean isModified() {
        return componentProvider != null && componentProvider.isModified();
    }

    @Override
    public void apply() {
        if (componentProvider != null) componentProvider.apply();
    }

    @Override
    public void disposeUIResources() {
        persistentState = null;
        componentProvider = null;
    }
}
