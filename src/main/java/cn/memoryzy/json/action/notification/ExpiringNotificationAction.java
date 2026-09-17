package cn.memoryzy.json.action.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * @author Memory
 * @since 2025/12/1
 */
public class ExpiringNotificationAction extends NotificationAction implements UpdateInBackground {

    private final BiConsumer<? super AnActionEvent, ? super Notification> myAction;

    public ExpiringNotificationAction(@Nullable String text, @NotNull BiConsumer<? super AnActionEvent, ? super Notification> myAction) {
        super(text);
        this.myAction = myAction;
    }

    public ExpiringNotificationAction(@Nullable String text, @NotNull Runnable action) {
        this(text, (event, notification) -> action.run());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        notification.expire();
        myAction.accept(e, notification);
    }
}
