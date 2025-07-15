package cn.memoryzy.json.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.model.deserializer.PluginUpdateDetail;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.VersionComparator;
import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        // 展示欢迎或更新通知
        showWelcomeOrUpdateNotification(project);
        // 实现公告（公告只会拉取、执行一次）
        AnnouncementManager.getInstance().scheduleDelayedAnnouncement(project);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // 合并旧配置
            ConfigurationMerger.getInstance().mergeProjectLegacySettings(project);
            // 检查有无更新
            checkForUpdates();
        });
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

    public void showWelcomeOrUpdateNotification(Project project) {
        // 获取版本
        String currentVersion = JsonAssistantPlugin.getVersion();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String lastVersion = propertiesComponent.getValue(JsonAssistantPlugin.PLUGIN_VERSION);

        if (lastVersion == null) {
            Notifications.showWelcomeNotification(project);
            propertiesComponent.setValue(JsonAssistantPlugin.PLUGIN_VERSION, currentVersion);
        } else {
            // 是否版本更高
            if (VersionComparator.isNewerVersion(lastVersion, currentVersion)) {
                Notifications.showUpdateNotification(project);
                propertiesComponent.setValue(JsonAssistantPlugin.PLUGIN_VERSION, currentVersion);
            }
        }
    }

    public void checkForUpdates() {
        // 获取插件市场的插件信息
        List<PluginUpdateDetail> pluginUpdateDetails = PlatformUtil.getPluginUpdateDetail();
        if (CollUtil.isEmpty(pluginUpdateDetails)) return;

        // 筛选出最新的一个版本
        PluginUpdateDetail pluginUpdateDetail = pluginUpdateDetails.get(0);
        String latestVersion = pluginUpdateDetail.getVersion();
        if (StrUtil.isBlank(latestVersion)) return;

        // 当前版本
        String currentVersion = JsonAssistantPlugin.getVersion();

        // 判断市场的最新版本是否大于当前版本
        JsonAssistantPlugin.setUpdateAvailable(
                VersionComparator.isNewerVersion(currentVersion, latestVersion),
                latestVersion,
                pluginUpdateDetail);
    }
}
