package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.Urls;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/3
 */
public class LatestVersionAction extends DumbAwareAction implements UpdateInBackground {

    public LatestVersionAction() {
        super(JsonAssistantBundle.messageOnSystem("action.latest.version.text"), JsonAssistantBundle.messageOnSystem("action.latest.version.description"), null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        long latestVersionId = JsonAssistantPlugin.getLatestVersionId();
        String url = latestVersionId > 0L
                ? StrUtil.format(Urls.MARKETPLACE_ASSIGN_VERSION_LINK, latestVersionId)
                : Urls.MARKETPLACE_VERSION_LINK;
        BrowserUtil.browse(url);
    }
}
