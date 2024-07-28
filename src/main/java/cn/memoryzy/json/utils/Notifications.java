package cn.memoryzy.json.utils;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.actions.child.DonateAction;
import cn.memoryzy.json.actions.child.QuickStartAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginDocument;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.toolWindow.ToolWindowPane;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.BalloonLayoutData;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

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


    @SuppressWarnings("deprecation")
    public static void showWelcomeNotification(Project project) {
        Notification notification = Notifications.BALLOON_LOG_GROUP
                .createNotification(
                        JsonAssistantBundle.messageOnSystem("notify.welcome.content",
                                PluginDocument.GITHUB_LINK,
                                PluginDocument.SPONSOR_LINK),
                        NotificationType.INFORMATION)
                .setTitle(JsonAssistantBundle.messageOnSystem("notify.welcome.title", JsonAssistantPlugin.getVersion()))
                .setImportant(true)
                .setListener(new NotificationListenerImpl())
                .addAction(new QuickStartAction())
                .addAction(new DonateAction(
                        JsonAssistantBundle.messageOnSystem("action.welcome.donate.text"),
                        PluginDocument.SUPPORT_LINK));

        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);
        if (window != null) {
            Balloon balloon = NotificationsManagerImpl.createBalloon(window,
                    notification,
                    false,
                    false,
                    BalloonLayoutData.fullContent(),
                    () -> System.out.println());

            JComponent component = window.getComponent();
            balloon.show(getUpperRightRelativePoint(component, (BalloonImpl) balloon), Balloon.Position.above);
        }

    }

    public static RelativePoint getUpperRightRelativePoint(JComponent component, BalloonImpl balloon) {
        // 在其他平台上，气球提示显示在标题栏的右侧边缘
        JLayeredPane layeredPane = component.getRootPane().getLayeredPane();
        Container contentPane = component.getRootPane().getContentPane();

        // 查找标题栏组件
        Component titleBar = Arrays.stream(layeredPane.getComponents())
                .filter(c -> c.getX() == 0 && c.getY() == 0 && c.getWidth() == layeredPane.getWidth() && c.getHeight() > 0)
                .findFirst()
                .orElse(null);

        int addWidth = 0;
        memory:
        for (Component contentComponent : contentPane.getComponents()) {
            if (contentComponent instanceof ToolWindowPane) {
                ToolWindowPane toolWindowPane = (ToolWindowPane) contentComponent;
                for (Component toolWindowPaneComponent : toolWindowPane.getComponents()) {
                    Object anchor = ReflectUtil.getFieldValue(toolWindowPaneComponent, "anchor");
                    if (Objects.equals(SwingConstants.RIGHT, anchor)) {
                        Object width = ReflectUtil.getFieldValue(toolWindowPaneComponent, "width");
                        if (width instanceof Integer) {
                            addWidth = (int) width;
                            break memory;
                        }
                    }
                }
            }
        }

        // 计算垂直偏移量
        int insetTop = balloon.getShadowBorderInsets().top;
        int contentHalfHeight = (int) (balloon.getContent().getPreferredSize().getHeight() / 2);
        int titleBarHeight = titleBar != null ? titleBar.getHeight() : 40;
        int offsetY = titleBarHeight + insetTop + contentHalfHeight;

        // 设置气球提示的显示位置
        Component relativeComponent = titleBar != null ? titleBar : component;

        int insetRight = balloon.getShadowBorderInsets().right;
        int contentHalfWidth = (int) (balloon.getContent().getPreferredSize().getWidth() / 2);
        int stripeRightWidth = addWidth > 0 ? addWidth : 25;
        int offsetX = relativeComponent.getWidth() - (stripeRightWidth + insetRight + contentHalfWidth);

        return new RelativePoint(relativeComponent, new Point(offsetX, offsetY));
    }


    private static class FullContentNotification extends Notification implements NotificationFullContent {
        public FullContentNotification(@NotNull @NonNls String groupId, @NotNull String title, @NotNull String content, @NotNull NotificationType type) {
            super(groupId, title, content, type);
        }
    }

    private static class NotificationListenerImpl extends NotificationListener.Adapter {
        @Override
        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
            URL url = e.getURL();
            BrowserUtil.browse(url.toExternalForm());
        }
    }
}
