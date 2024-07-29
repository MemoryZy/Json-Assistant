package cn.memoryzy.json.listener;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.utils.Notifications;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class PluginWelcomeAndUpdateManager implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 验证地址可达性
        HyperLinks.verifyReachable();

        // 获取版本
        String currentVersion = JsonAssistantPlugin.getVersion();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String lastVersion = propertiesComponent.getValue(JsonAssistantPlugin.PLUGIN_VERSION);

        // 如果 lastVersion 为 null，表示第一次安装，todo 测试一下unload又load的情况
        if (lastVersion == null) {
            Notifications.showWelcomeNotification(project);
            propertiesComponent.setValue(JsonAssistantPlugin.PLUGIN_VERSION, currentVersion);
            return;
        }

        // 是否版本更高



    }
}
