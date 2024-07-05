package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author wcp
 * @since 2024/7/5
 */
public class DonateOnTwTitleAction extends AnAction {

    public static final String SUPPORT_LINK = "https://json.memoryzy.cn/support";

    public DonateOnTwTitleAction() {
        super(JsonAssistantBundle.messageOnSystem("action.donate.text"),
                JsonAssistantBundle.messageOnSystem("action.donate.description"),
                JsonAssistantIcons.DONATE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse(SUPPORT_LINK);
    }
}
