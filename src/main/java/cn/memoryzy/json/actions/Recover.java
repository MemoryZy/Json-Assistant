package cn.memoryzy.json.actions;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 * @author Memory
 * @since 2024/7/26
 */
public class Recover extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.unsetValue(JsonAssistantPlugin.PLUGIN_VERSION);
    }
}
