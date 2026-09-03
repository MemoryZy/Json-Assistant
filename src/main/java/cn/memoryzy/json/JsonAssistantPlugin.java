package cn.memoryzy.json;

import cn.memoryzy.json.model.deserializer.PluginUpdateDetail;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
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

    public static final String PLUGIN_AUTHOR = "Memory";

    public static final String PLUGIN_ID = "cn.memoryzy.json";
    public static final String PLUGIN_NAME = "Json Assistant";
    public static final String PLUGIN_ID_NAME = PLUGIN_ID + ".Json-Assistant";
    public static final String PLUGIN_VERSION = PLUGIN_ID + ".version";

    public static final String STORAGE_MAIN_FILE = "json_assistant_settings.xml";
    public static final String STORAGE_HISTORY_FILE = "json_assistant_history.xml";

    public static final Integer CONFIG_VERSION = 1;
    public static final boolean LEGACY_FLOATING_TOOLBAR_PROVIDER = PlatformUtil.isLegacyFloatingToolbarProvider();

    /**
     * 使用一个原子引用持有不可变的状态对象
     */
    private static final AtomicReference<UpdateInfo> stateHolder =
            new AtomicReference<>(new UpdateInfo(false, null, null));


    public static IdeaPluginDescriptor getJsonAssistantPlugin() {
        return (IdeaPluginDescriptor) JsonAssistantUtil.invokeStaticMethod(PluginManagerCore.class, "getPlugin", PluginId.getId(PLUGIN_ID));
    }

    public static String getVersion() {
        return getJsonAssistantPlugin().getVersion();
    }


    // -------------------------------------

    public static void setUpdateAvailable(boolean hasUpdateAvailable, String latestVersion, PluginUpdateDetail updateDetail) {
        // 创建新状态对象并原子更新
        stateHolder.set(new UpdateInfo(hasUpdateAvailable, latestVersion, updateDetail));
    }

    public static long getLatestVersionId() {
        return Optional.ofNullable(stateHolder.get().pluginUpdateDetail).map(PluginUpdateDetail::getId).orElse(0L);
    }


    public static boolean hasUpdateAvailable() {
        return stateHolder.get().updateAvailable;
    }

    public static String getLatestVersion() {
        return stateHolder.get().latestVersion;
    }


    public static String getLatestChineseChangeNotes() {
        return Optional.ofNullable(stateHolder.get().pluginUpdateDetail).map(PluginUpdateDetail::getZhNotes).orElse("");
    }

    public static String getLatestEnglishChangeNotes() {
        return Optional.ofNullable(stateHolder.get().pluginUpdateDetail).map(PluginUpdateDetail::getEnNotes).orElse("");
    }



    /**
     * 不可变状态对象
     */
    private static class UpdateInfo {
        final boolean updateAvailable;
        final String latestVersion;
        final PluginUpdateDetail pluginUpdateDetail;

        public UpdateInfo(boolean updateAvailable, String latestVersion, PluginUpdateDetail pluginUpdateDetail) {
            this.updateAvailable = updateAvailable;
            this.latestVersion = latestVersion;
            this.pluginUpdateDetail = pluginUpdateDetail;
        }
    }

}
