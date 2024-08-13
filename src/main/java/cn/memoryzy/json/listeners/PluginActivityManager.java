package cn.memoryzy.json.listeners;

import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.utils.Notifications;
import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class PluginActivityManager implements StartupActivity, DynamicPluginListener {

    /**
     * 项目打开，并在索引建立完后执行
     *
     * @param project 项目对象
     */
    @Override
    public void runActivity(@NotNull Project project) {
        // 验证地址可达性
        HyperLinks.verifyReachable();

        // 获取版本
        String currentVersion = JsonAssistantPlugin.getVersion();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String lastVersion = propertiesComponent.getValue(JsonAssistantPlugin.PLUGIN_VERSION);

        if (lastVersion == null) {
            Notifications.showWelcomeNotification(project);
            propertiesComponent.setValue(JsonAssistantPlugin.PLUGIN_VERSION, currentVersion);
        } else {
            // 是否版本更高
            if (JsonAssistantPlugin.isNewerVersion(lastVersion, currentVersion)) {
                Notifications.showUpdateNotification(project);
                propertiesComponent.setValue(JsonAssistantPlugin.PLUGIN_VERSION, currentVersion);
            }
        }
    }

    /**
     * 插件 Unload 前执行（uninstall 不执行）
     *
     * @param pluginDescriptor 插件详情
     * @param isUpdate         如果插件作为更新安装的一部分被卸载，并且之后将加载新版本，则为true，反之为false
     */
    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        if (!isUpdate) {
            PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
            propertiesComponent.unsetValue(JsonAssistantPlugin.PLUGIN_VERSION);
        }
    }
}
