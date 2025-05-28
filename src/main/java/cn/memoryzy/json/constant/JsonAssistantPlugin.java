package cn.memoryzy.json.constant;

import com.intellij.diagnostic.PluginException;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class JsonAssistantPlugin {

    public static final String PLUGIN_ID = "cn.memoryzy.json";
    public static final String PLUGIN_NAME = "Json Assistant";
    public static final String PLUGIN_ID_NAME = PLUGIN_ID + ".Json-Assistant";
    public static final String PLUGIN_VERSION = PLUGIN_ID + ".version";

    private static final IdeaPluginDescriptor descriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));

    public static IdeaPluginDescriptor getJsonAssistant() {
        if (descriptor == null) {
            throw new PluginException("Plugin does not exist!", PluginId.getId(PLUGIN_ID));
        }

        return descriptor;
    }

    public static String getVersion() {
        return getJsonAssistant().getVersion();
    }
}
