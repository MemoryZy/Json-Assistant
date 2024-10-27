package cn.memoryzy.json.extension.configurable;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.UrlEnum;
import cn.memoryzy.json.ui.JsonAssistantMainConfigurableComponentProvider;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/13
 */
public class JsonAssistantMainConfigurable implements Configurable {
    private JsonAssistantMainConfigurableComponentProvider componentProvider;

    @Override
    public String getDisplayName() {
        return JsonAssistantBundle.message("plugin.main.configurable.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (componentProvider == null) componentProvider = new JsonAssistantMainConfigurableComponentProvider();
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

    @Override
    public @Nullable @NonNls String getHelpTopic() {
        return UrlEnum.DEFAULT.getId();
    }
}
