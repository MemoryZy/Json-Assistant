package cn.memoryzy.json.utils;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/7/3
 */
public class Notifications {

    /**
     * 获取注册的通知组
     */
    public static final NotificationGroup BALLOON_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("JsonAssistant Plugin");

    public static final NotificationGroup BALLOON_LOG_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("JsonAssistant Plugin Log");

    /**
     * 展示通知（瞬态通知）
     */
    public static void showNotification(String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON_GROUP.createNotification(content, notificationType).notify(project);
    }

    /**
     * 展示通知（瞬态通知）
     */
    public static void showNotification(String title, String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON_GROUP.createNotification(content, notificationType).setTitle(title).notify(project);
    }

    /**
     * 展示通知（通知记录在 Event Log 或 Notifications 中）
     */
    public static void showLogNotification(String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON_LOG_GROUP.createNotification(content, notificationType).notify(project);
    }

    /**
     * 展示通知（通知记录在 Event Log 或 Notifications 中）
     */
    public static void showLogNotification(String title, String content, NotificationType notificationType, Project project) {
        // 使用通知组创建通知
        BALLOON_LOG_GROUP.createNotification(content, notificationType).setTitle(title).notify(project);
    }

    /**
     * 展示不被折叠的通知
     */
    public static void showFullNotification(String title, String content, NotificationType notificationType, Project project) {
        new FullContentNotification(BALLOON_LOG_GROUP.getDisplayId(), title, content, notificationType).notify(project);
    }

    public static void showWelcomeNotification() {
        Notification notification = BALLOON_LOG_GROUP
                .createNotification("感谢你的使用！\n" +
                                "<b><a href=\"https://github.com/MemoryZy/Json-Assistant\">Json Assistant</a></b> &nbsp;是一款开源插件，它源于个人兴趣与使用需求，我将会长期维护它。\n" +
                                "如果你觉得 <b>Json Assistant</b> 对你有帮助的话，请考虑给予 <b><a href=\"https://json.memoryzy.cn/support\">捐赠</a></b>，" +
                                "你的支持将极大地鼓励我继续改进和维护这个项目。<br/><br/>",
                        NotificationType.INFORMATION)
                .setTitle(JsonAssistantBundle.messageOnSystem("notify.welcome.title", JsonAssistantPlugin.getVersion()));

    }

    private static class FullContentNotification extends Notification implements NotificationFullContent {
        public FullContentNotification(@NotNull @NonNls String groupId, @NotNull String title, @NotNull String content, @NotNull NotificationType type) {
            super(groupId, title, content, type);
        }
    }

}
