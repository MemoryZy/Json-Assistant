package cn.memoryzy.json;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.model.PluginDetail;
import com.intellij.diagnostic.PluginException;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * 使用一个原子引用持有不可变的状态对象
     */
    private static final AtomicReference<UpdateInfo> stateHolder =
            new AtomicReference<>(new UpdateInfo(false, null, null));

    public static IdeaPluginDescriptor getJsonAssistant() {
        if (descriptor == null) {
            throw new PluginException("Plugin does not exist!", PluginId.getId(PLUGIN_ID));
        }

        return descriptor;
    }

    public static String getVersion() {
        return getJsonAssistant().getVersion();
    }


    // -------------------------------------

    public static void setUpdateAvailable(boolean hasUpdateAvailable, String latestVersion, PluginDetail pluginDetail) {
        // 创建新状态对象并原子更新
        stateHolder.set(new UpdateInfo(hasUpdateAvailable, latestVersion, pluginDetail));
    }


    public static boolean hasUpdateAvailable() {
        return stateHolder.get().updateAvailable;
    }

    public static String getLatestVersion() {
        return stateHolder.get().latestVersion;
    }

    public static String getLatestChineseChangeNotes() {
        PluginDetail pluginDetail = stateHolder.get().pluginDetail;
        return Optional.ofNullable(pluginDetail)
                .map(PluginDetail::getCategory)
                .map(PluginDetail.Category::getIdeaPlugins)
                .filter(CollUtil::isNotEmpty)
                .map(list -> list.get(0))
                .map(PluginDetail.IdeaPlugin::getChineseChangeNotes)
                .orElse(null);
    }

    public static String getLatestEnglishChangeNotes() {
        PluginDetail pluginDetail = stateHolder.get().pluginDetail;
        return Optional.ofNullable(pluginDetail)
                .map(PluginDetail::getCategory)
                .map(PluginDetail.Category::getIdeaPlugins)
                .filter(CollUtil::isNotEmpty)
                .map(list -> list.get(0))
                .map(PluginDetail.IdeaPlugin::getEnglishChangeNotes)
                .orElse(null);
    }



    /**
     * 不可变状态对象
     */
    private static class UpdateInfo {
        final boolean updateAvailable;
        final String latestVersion;
        final PluginDetail pluginDetail;

        public UpdateInfo(boolean updateAvailable, String latestVersion, PluginDetail pluginDetail) {
            this.updateAvailable = updateAvailable;
            this.latestVersion = latestVersion;
            this.pluginDetail = pluginDetail;
        }
    }

}
