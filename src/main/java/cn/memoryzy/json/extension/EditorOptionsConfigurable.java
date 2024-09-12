package cn.memoryzy.json.extension;

import cn.memoryzy.json.ui.EditorOptionsComponentProvider;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class EditorOptionsConfigurable implements Configurable {

    private EditorOptionsComponentProvider componentProvider;

    @Override
    public String getDisplayName() {
        return "Json Viewer";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (componentProvider == null) componentProvider = new EditorOptionsComponentProvider();
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
        componentProvider = null;
    }
}
