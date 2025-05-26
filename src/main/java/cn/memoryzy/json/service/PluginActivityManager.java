package cn.memoryzy.json.service;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.util.Notifications;
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
        Urls.verifyReachable();

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

        // TODO 用异步实现公告

        // TODO 通告
        //  1. 唯一标识 2.

        // https://raw.githubusercontent.com/MemoryZy/Json-Assistant/refs/heads/main/ANNOUNCEMENTS

        // https://gitee.com/MemoryZy/Json-Assistant/raw/main/ANNOUNCEMENTS


        /*
        [
  {
    // 唯一标识（必填）
    "id": "202308_update_v2",
    "title": "重要更新通知",                 // 标题（必填）
    "content": "本次更新新增了XX功能...",     // 正文（必填）
    "type": "warning",                     // 公告类型（info/warning/error）
    "priority": 1,                         // 显示优先级（数值越大越优先）
    "effectiveDate": "2023-08-25",          // 生效日期（ISO8601）
    "expirationDate": "2023-09-30",         // 过期日期（自动隐藏）
    "versionConstraints": ">=1.2.0",       // 版本约束（语义化版本范围）
    "isDismissible": true,                  // 是否允许用户关闭
    "actions": [                            // 关联操作按钮
      {
        "label": "查看详情",
        "url": "https://example.com/docs"
      },
      {
        "label": "立即升级",
        "command": "updatePlugin"
      }
    ],
    "metadata": {                          // 扩展元数据
      "author": "Dev Team",
      "createdAt": "2023-08-20T14:30:00Z"
    }
  }
]

         */

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
