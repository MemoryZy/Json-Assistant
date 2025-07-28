package cn.memoryzy.json.extension.configurable;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.UrlType;
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

    public static final String ID = "JsonAssistant.Configurable.JsonAssistantMainConfigurable";

    @Override
    public String getDisplayName() {
        return JsonAssistantBundle.message("setting.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (componentProvider == null) componentProvider = new JsonAssistantMainConfigurableComponentProvider();
        return componentProvider.createComponent();
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
        return UrlType.DEFAULT.getId();
    }
}
