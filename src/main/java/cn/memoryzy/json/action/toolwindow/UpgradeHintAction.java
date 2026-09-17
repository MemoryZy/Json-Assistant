package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class UpgradeHintAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    @SuppressWarnings("DialogTitleCapitalization")
    public UpgradeHintAction() {
        super("Updates available", JsonAssistantBundle.messageOnSystem("action.upgrade.description"), JsonAssistantIcons.UPGRADE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtilImpl.showSettingsDialog(e.getProject(), PluginManagerConfigurable.ID, JsonAssistantPlugin.PLUGIN_NAME);

        // 想要手动下载插件包的话，需要用 "https://plugins.jetbrains.com/files/" 的前缀
        // 再加上插件更新信息 (PluginUpdateDetail) 的 file 字段，如 "24738/716957/Json_Assistant-1.8.0.zip"
        // 合起来就是 "https://plugins.jetbrains.com/files/24738/716957/Json_Assistant-1.8.0.zip"
        // 或者 "https://downloads.marketplace.jetbrains.com/files/24738/716957/Json_Assistant-1.8.0.zip"

        // 可以本地直接下载最新插件包，再用 PluginInstaller.installAfterRestart(); 指定安装，但是需要配合进度条
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        boolean chineseLocale = PlatformUtil.isChineseLocale();
        String latestVersion = JsonAssistantPlugin.getLatestVersion();
        String description = chineseLocale
                ? JsonAssistantPlugin.getLatestChineseChangeNotes()
                : JsonAssistantPlugin.getLatestEnglishChangeNotes();

        if (StrUtil.isNotBlank(description)) {
            description =
                    StrUtil.format("<br/><b>{}</b><br/>", chineseLocale ? "更新内容如下：" : "The updated content is as follows::")
                            + description;
        }

        String finalDescription = description;
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                new HelpTooltip()
                        .setTitle(JsonAssistantBundle.messageOnSystem("action.upgrade.text", latestVersion))
                        .setDescription(finalDescription)
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(Objects.nonNull(e.getProject()) && JsonAssistantPlugin.hasUpdateAvailable());
    }
}
