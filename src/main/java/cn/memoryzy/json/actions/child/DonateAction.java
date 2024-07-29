package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
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

    private final String hyperLink;

    public DonateAction(String text, String hyperLink) {
        super(text, JsonAssistantBundle.messageOnSystem("action.donate.description"), JsonAssistantIcons.DONATE);
        this.hyperLink = hyperLink;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (HyperLinks.reachableAtomic.get()) {
            BrowserUtil.browse(hyperLink);
        } else {
            // 构建弹窗


        }
    }

}
