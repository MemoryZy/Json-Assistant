package cn.memoryzy.json.utils;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.actions.child.DonateAction;
import cn.memoryzy.json.actions.child.QuickStartAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.ui.SupportDialog;
import cn.memoryzy.json.ui.basic.CustomizedUI;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.BalloonLayoutData;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Arrays;

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


    @SuppressWarnings({"deprecation", "DuplicatedCode"})
    public static void showWelcomeNotification(Project project) {
        Notification notification = Notifications.BALLOON_LOG_GROUP
                .createNotification(
                        JsonAssistantBundle.messageOnSystem("notify.welcome.content",
                                HyperLinks.GITHUB_LINK, HyperLinks.PLUGIN_SPONSOR_LINK) + "<br/>",
                        NotificationType.INFORMATION)
                .setTitle(JsonAssistantBundle.messageOnSystem("notify.welcome.title", JsonAssistantPlugin.getVersion()))
                .setImportant(true)
                .setListener(new NotificationListenerImpl())
                .addAction(new QuickStartAction())
                .addAction(new DonateAction(JsonAssistantBundle.messageOnSystem("action.welcome.donate.text")));

        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);
        if (window != null) {
            Balloon balloon = NotificationsManagerImpl.createBalloon(window,
                    notification,
                    false,
                    false,
                    BalloonLayoutData.fullContent(),
                    new CustomizedUI());

            JComponent component = window.getComponent();
            balloon.show(getUpperRightRelativePoint(component, (BalloonImpl) balloon), Balloon.Position.above);
        }
    }


    @SuppressWarnings({"DuplicatedCode", "deprecation"})
    public static void showUpdateNotification(Project project) {
        String changeNotes = JsonAssistantPlugin.getJsonAssistant().getChangeNotes();
        if (StrUtil.isBlank(changeNotes)) {
            changeNotes = "<ul></ul>";
        }

        String content = JsonAssistantBundle.messageOnSystem("notify.welcome.content", HyperLinks.GITHUB_LINK, HyperLinks.PLUGIN_SPONSOR_LINK);
        content += "<br/>" + JsonAssistantBundle.messageOnSystem("notify.update.content", changeNotes);

        Notification notification = Notifications.BALLOON_LOG_GROUP
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(JsonAssistantBundle.messageOnSystem("notify.update.title", JsonAssistantPlugin.getVersion()))
                .setImportant(true)
                .setListener(new NotificationListenerImpl())
                .addAction(new QuickStartAction())
                .addAction(new DonateAction(JsonAssistantBundle.messageOnSystem("action.welcome.donate.text")));

        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);
        if (window != null) {
            Balloon balloon = NotificationsManagerImpl.createBalloon(window,
                    notification,
                    false,
                    false,
                    BalloonLayoutData.fullContent(),
                    new CustomizedUI());

            JComponent component = window.getComponent();
            balloon.show(getUpperRightRelativePoint(component, (BalloonImpl) balloon), Balloon.Position.above);
        }
    }


    public static RelativePoint getUpperRightRelativePoint(JComponent component, BalloonImpl balloon) {
        // 在其他平台上，气球提示显示在标题栏的右侧边缘
        JLayeredPane layeredPane = component.getRootPane().getLayeredPane();

        // 查找标题栏组件
        Component titleBar = Arrays.stream(layeredPane.getComponents())
                .filter(c -> c.getX() == 0 && c.getY() == 0 && c.getWidth() == layeredPane.getWidth() && c.getHeight() > 0)
                .findFirst()
                .orElse(null);

        // 计算垂直偏移量
        int insetTop = balloon.getShadowBorderInsets().top;
        int contentHalfHeight = (int) (balloon.getContent().getPreferredSize().getHeight() / 2);
        int titleBarHeight = titleBar != null ? titleBar.getHeight() : 40;
        int offsetY = titleBarHeight + insetTop + contentHalfHeight;

        // 设置气球提示的显示位置
        Component relativeComponent = titleBar != null ? titleBar : component;

        int insetRight = balloon.getShadowBorderInsets().right;
        int contentHalfWidth = (int) (balloon.getContent().getPreferredSize().getWidth() / 2);
        int offsetX = relativeComponent.getWidth() - (25 + insetRight + contentHalfWidth);

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
            String url = e.getDescription();

            if (url.equals(HyperLinks.PLUGIN_SPONSOR_LINK)) {
                if (HyperLinks.isReachable()) {
                    BrowserUtil.browse(HyperLinks.SPONSOR_LINK);
                } else {
                    SupportDialog dialog = new SupportDialog();
                    ApplicationManager.getApplication().invokeLater(dialog::show);
                }
            } else {
                BrowserUtil.browse(url);
            }
        }
    }
}
