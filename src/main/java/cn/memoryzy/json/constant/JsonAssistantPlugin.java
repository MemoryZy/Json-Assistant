package cn.memoryzy.json.constant;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class JsonAssistantPlugin {

    public static final String PLUGIN_ID = "cn.memoryzy.json";
    public static final String PLUGIN_ID_NAME = PLUGIN_ID + ".Json-Assistant";
    public static final String PLUGIN_VERSION = PLUGIN_ID + ".version";

    public static final IdeaPluginDescriptor descriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));

    public static String getVersion() {
        return Objects.isNull(descriptor) ? null : descriptor.getVersion();
    }

}
