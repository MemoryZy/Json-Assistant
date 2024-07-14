package cn.memoryzy.json.utils;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author Memory
 * @since 2024/7/3
 */
public class Notification {

    /**
     * 获取通知组管理器
     */
    private static final NotificationGroupManager MANAGER = NotificationGroupManager.getInstance();

    /**
     * 获取注册的通知组
     */
    public static final NotificationGroup BALLOON = MANAGER.getNotificationGroup("MemoryZy.JsonAssistant.Notify.WithoutLog.Balloon");

    public static final NotificationGroup BALLOON_WITH_LOG = MANAGER.getNotificationGroup("MemoryZy.JsonAssistant.Notify.WithLog.Balloon");

    /**
     * 给定程序通知
     *
     * @param content          内容
     * @param notificationType 通知级别
     */
    public static void notify(String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON.createNotification(content, notificationType).notify(project);
    }

    public static void notifyLog(String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON_WITH_LOG.createNotification(content, notificationType).notify(project);
    }

}
