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
    public static final NotificationGroup BALLOON = MANAGER.getNotificationGroup("MemoryZy.JsonAssistant.notify.Balloon");

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

}
