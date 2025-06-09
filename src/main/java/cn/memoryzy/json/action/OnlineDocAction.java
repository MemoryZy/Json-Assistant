package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.Notifications;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/6
 */
public class OnlineDocAction extends DumbAwareAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(OnlineDocAction.class);

    public OnlineDocAction() {
        super(JsonAssistantBundle.messageOnSystem("action.online.doc.text"), JsonAssistantBundle.messageOnSystem("action.online.doc.description"), JsonAssistantIcons.BOOK_READER);
    }

    public OnlineDocAction(String text, Icon icon) {
        super(text, JsonAssistantBundle.messageOnSystem("action.online.doc.description"), icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // TODO 需要注意的位置
        // PlatformUtil.openOnlineDoc(event.getProject(), true);
        Project project = event.getProject();
print("start");
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "Convert", true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                print("start2");
                // indicator.setText("start2");
                indicator.setText("start3");
                indicator.setFraction(0.1);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                indicator.setFraction(0.8);
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.runtime.serialize.recursion"), NotificationType.ERROR, project);

                indicator.setFraction(1.0);
                indicator.setText("Finished");
                print("end2");
            }
        };

        backgroundable.queue();

    }

    private void print(String text) {
        String formatted = StrUtil.format("[{}] -- {}", Thread.currentThread().getName(), text);


        System.out.println(formatted);


    }

}
