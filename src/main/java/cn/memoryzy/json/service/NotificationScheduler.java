package cn.memoryzy.json.service;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationScheduler {

    public static NotificationScheduler getInstance() {
        return ApplicationManager.getApplication().getService(NotificationScheduler.class);
    }

    private final Queue<Notification> notificationQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isShowing = new AtomicBoolean(false);
    private final AtomicInteger delayMillis = new AtomicInteger(3000);

    public void setDelay(int millis) {
        delayMillis.set(millis);
    }

    public void addNotification(Notification notification) {
        notificationQueue.offer(notification);
        tryShowNext();
    }

    private void tryShowNext() {
        if (isShowing.compareAndSet(false, true)) {
            showNext();
        }
    }

    private void showNext() {
        Notification next = notificationQueue.poll();
        if (next == null) {
            isShowing.set(false);
            return;
        }

        Notifications.Bus.notify(next);

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Thread.sleep(delayMillis.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            showNext();
        });
    }
}
