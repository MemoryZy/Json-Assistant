package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class OnlineDocAction extends DumbAwareAction implements UpdateInBackground {

    public OnlineDocAction() {
        super(JsonAssistantBundle.messageOnSystem("action.online.doc.text"), JsonAssistantBundle.messageOnSystem("action.online.doc.description"), JsonAssistantIcons.BOOK_READER);
    }

    public OnlineDocAction(String text, Icon icon) {
        super(text, JsonAssistantBundle.messageOnSystem("action.online.doc.description"), icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PlatformUtil.openOnlineDoc(event.getProject(), true);
    }

}
