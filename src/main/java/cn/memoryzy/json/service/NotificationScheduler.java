package cn.memoryzy.json.service;

import cn.memoryzy.json.util.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationScheduler implements Disposable {

    private static final Logger LOG = Logger.getInstance(NotificationScheduler.class);

    public static NotificationScheduler getInstance() {
        return ApplicationManager.getApplication().getService(NotificationScheduler.class);
    }

    private final Queue<Notifications.FullContentNotification> notificationQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isShowing = new AtomicBoolean(false);
    private final AtomicInteger delayMillis = new AtomicInteger(4000);

    public void setDelay(int millis) {
        delayMillis.set(millis);
    }

    public void addNotifications(List<Notifications.FullContentNotification> notifications, @NotNull Project project) {
        if (project.isDisposed()) return;
        // 直接操作队列（线程安全）
        notificationQueue.addAll(notifications);
        // 尝试立即显示（非阻塞）
        tryShowNext(project);
    }

    private void tryShowNext(@NotNull Project project) {
        if (isShowing.compareAndSet(false, true)) {
            showNext(project);
        }
    }

    private void showNext(@NotNull Project project) {
        try {
            Notifications.FullContentNotification next = notificationQueue.poll();
            if (next == null || project.isDisposed()) {
                isShowing.set(false);
                return;
            }

            // 通知关闭事件
            next.setCloseRunnable(() -> {
                // 清理资源
                next.dispose();
                startDelayTimer(project);
                // TODO 还需要往Map中添加已读

            });

            // 启动通知
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    next.notify(project);
                } catch (Exception e) {
                    handleNotificationError(project, e);
                }
            });
        } catch (Exception e) {
            handleNotificationError(project, e);
        }
    }

    private void startDelayTimer(@NotNull Project project) {
        // 唯一线程池入口
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // 延迟控制
                Thread.sleep(delayMillis.get());

                // 切回EDT显示下一个
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!project.isDisposed()) {
                        showNext(project);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void handleNotificationError(@NotNull Project project, Exception e) {
        isShowing.set(false);
        if (!project.isDisposed()) {
            LOG.warn("通知处理失败", e);
        }
    }

    @Override
    public void dispose() {
        notificationQueue.clear();
    }
}
