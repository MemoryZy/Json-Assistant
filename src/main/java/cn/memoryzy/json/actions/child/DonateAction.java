package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginDocument;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class DonateAction extends DumbAwareAction {

    public DonateAction() {
        super(JsonAssistantBundle.messageOnSystem("action.donate.text"),
                JsonAssistantBundle.messageOnSystem("action.donate.description"),
                JsonAssistantIcons.DONATE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (PluginDocument.reachableAtomic.get()) {
            BrowserUtil.browse(PluginDocument.SUPPORT_LINK);
        } else {
            // 构建弹窗


        }
    }

}
