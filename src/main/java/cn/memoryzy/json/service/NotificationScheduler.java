package cn.memoryzy.json.service;

import cn.memoryzy.json.util.Notifications;
import com.intellij.notification.Notification;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.intellij.util.AlarmFactory;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Memory
 * @since 2025/6/1
 */
@Service(Service.Level.APP)
public final class NotificationScheduler implements Disposable {

    private static final Logger LOG = Logger.getInstance(NotificationScheduler.class);

    public static NotificationScheduler getInstance() {
        return ApplicationManager.getApplication().getService(NotificationScheduler.class);
    }

    private final Queue<Notification> notificationQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isShowing = new AtomicBoolean(false);
    private final AtomicInteger delayMillis = new AtomicInteger(60 * 1000);
    private final Alarm alarm = AlarmFactory.getInstance().create(Alarm.ThreadToUse.POOLED_THREAD, this);

    public void setDelay(int millis) {
        delayMillis.set(millis);
    }

    public void addNotifications(List<Notification> notifications, Consumer<String> consumer, @NotNull Project project) {
        // 直接操作队列（线程安全）
        notificationQueue.addAll(notifications);
        // 尝试立即显示（非阻塞）
        tryShowNext(project, consumer);
    }

    private void tryShowNext(@NotNull Project project, Consumer<String> consumer) {
        if (isShowing.compareAndSet(false, true)) {
            showNext(project, consumer);
        }
    }

    private void showNext(@NotNull Project project, Consumer<String> consumer) {
        if (project.isDisposed()) {
            isShowing.set(false);
            return;
        }

        Notification next = notificationQueue.poll();
        if (next == null) {
            isShowing.set(false);
            return;
        }

        // 设置通知关闭回调
        if (next instanceof Notifications.FullContentNotification) {
            Notifications.FullContentNotification notification = (Notifications.FullContentNotification) next;
            notification.setClosedIdConsumer(id -> {
                scheduleNext(project, consumer);
                if (null != consumer) {
                    consumer.consume(id);
                }
            });
        }

        // 启动通知
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                next.notify(project);
            } catch (Exception e) {
                handleNotificationError(project, e);
            }
        });
    }

    private void scheduleNext(@NotNull Project project, Consumer<String> consumer) {
        if (notificationQueue.isEmpty()) {
            handleEmptyQueue();
            return;
        }

        // 延迟执行下一个通知（非阻塞）
        alarm.addRequest(() -> {
            if (project.isDisposed() || notificationQueue.isEmpty()) {
                isShowing.set(false);
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                // 第二层判断：确保执行时仍有元素
                if (!notificationQueue.isEmpty()) {
                    showNext(project, consumer);
                } else {
                    isShowing.set(false);
                }
            });

        }, delayMillis.get());
    }

    private void handleNotificationError(@NotNull Project project, Exception e) {
        isShowing.set(false);
        if (!project.isDisposed()) {
            LOG.warn("[Json Assistant] Notification processing failed", e);
        }
    }

    private void handleEmptyQueue() {
        isShowing.set(false);
        alarm.cancelAllRequests(); // 彻底清理残留任务
    }

    @Override
    public void dispose() {
        alarm.cancelAllRequests();
        notificationQueue.clear();
    }
}
