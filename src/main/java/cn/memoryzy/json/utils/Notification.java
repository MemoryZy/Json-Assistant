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
    public static final NotificationGroup BALLOON = MANAGER.getNotificationGroup("JsonAssistant Plugin");

    public static final NotificationGroup BALLOON_WITH_LOG = MANAGER.getNotificationGroup("JsonAssistant Plugin With Log");

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

    // public static void customNotification(Project project) {
    //     com.intellij.notification.Notification notification = BALLOON_WITH_LOG.createNotification("被忽略的属性：<div style=\"margin-top: -50px\">" +
    //                     "  <ul>" +
    //                     "    <li>name</li>" +
    //                     "    <li>age</li>" +
    //                     "    <li>salary</li>" +
    //                     "  </ul></div>", NotificationType.INFORMATION)
    //
    //             .setImportant(true);
    //
    //     notification.notify(project);
    //
    //     int scale = JBUI.scale(3);
    //
    //     System.out.println();

    // IdeFrame window = (IdeFrame)NotificationsManagerImpl.findWindowForBalloon(project);
    //
    // Balloon balloon = NotificationsManagerImpl.createBalloon(window,
    //         notification, true, false,
    //         BalloonLayoutData.fullContent(), new Disposable() {
    //             @Override
    //             public void dispose() {
    //                 System.out.println("释放");
    //             }
    //         });
    //
    // JComponent component = window.getComponent();
    //
    // balloon.show(get2(component, (BalloonImpl) balloon), Balloon.Position.above);


    // DumbAwareAction dumbAwareAction = new DumbAwareAction("check") {
    //     @Override
    //     public void actionPerformed(@NotNull AnActionEvent e) {
    //
    //     }
    // };
    //
    // notification.addAction(dumbAwareAction);
    //
    // notification.notify(project);
    // }


    // public static RelativePoint getTarget(Component component) {
    //     int x = component.getWidth();
    //     int y = component.getHeight();
    //
    //     System.out.println("x: " + x + ", y: " + y);
    //     return new RelativePoint(component, new Point(x, y));
    // }
    //
    // public static RelativePoint get2(JComponent component, BalloonImpl balloon){
    //     // 在其他平台上，气球提示显示在标题栏的右侧边缘
    //     // 获取分层窗格
    //
    //     JLayeredPane layeredPane = component.getRootPane().getLayeredPane();
    //     // 查找标题栏组件
    //     Component titleBar = Arrays.stream(layeredPane.getComponents())
    //             .filter(c -> c.getX() == 0 && c.getY() == 0 && c.getWidth() == layeredPane.getWidth() && c.getHeight() > 0)
    //             .findFirst()
    //             .orElse(null);
    //
    //     // 计算垂直偏移量
    //     int insetTop = balloon.getShadowBorderInsets().top;
    //     int contentHalfHeight = (int) (balloon.getContent().getPreferredSize().getHeight() / 2);
    //     int titleBarHeight = titleBar != null ? titleBar.getHeight() : 40;
    //     int offsetY = titleBarHeight + insetTop + contentHalfHeight;
    //
    //     // 设置气球提示的显示位置
    //     Component relativeComponent = titleBar != null ? titleBar : component;
    //     return new RelativePoint(relativeComponent, new Point(relativeComponent.getWidth(), offsetY));
    // }
}
