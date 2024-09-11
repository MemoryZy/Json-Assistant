package cn.memoryzy.json.extension;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class EditorOptionsConfigurable implements Configurable {

    @Override
    public String getDisplayName() {
        return JsonAssistantPlugin.PLUGIN_NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
