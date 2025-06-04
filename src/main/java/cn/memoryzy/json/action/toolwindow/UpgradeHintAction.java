package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.HelpTooltip;
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

public class UpgradeHintAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    @SuppressWarnings("DialogTitleCapitalization")
    public UpgradeHintAction() {
        super("Updates available", JsonAssistantBundle.messageOnSystem("action.upgrade.description"), JsonAssistantIcons.UPGRADE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO 待实现

    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        boolean chineseLocale = PlatformUtil.isChineseLocale();
        String latestVersion = JsonAssistantPlugin.getLatestVersion();
        String description = chineseLocale
                ? JsonAssistantPlugin.getLatestChineseChangeNotes()
                : JsonAssistantPlugin.getLatestEnglishChangeNotes();

        String pre = StrUtil.format("<br/><b>{}</b><br/>",
                chineseLocale ? "更新内容如下：" : "The updated content is as follows::");

        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                new HelpTooltip()
                        .setTitle(JsonAssistantBundle.messageOnSystem("action.upgrade.text", latestVersion))
                        .setDescription(pre + description)
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(JsonAssistantPlugin.hasUpdateAvailable());
    }
}
