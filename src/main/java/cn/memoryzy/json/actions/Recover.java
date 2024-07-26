package cn.memoryzy.json.actions;

import cn.memoryzy.json.actions.notify.QuickStartAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginDocument;
import cn.memoryzy.json.utils.Notifications;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.BalloonLayoutData;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Memory
 * @since 2024/7/26
 */
public class Recover extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        // propertiesComponent.unsetValue(JsonAssistantPlugin.PLUGIN_VERSION);



        customNotification(e.getProject());
    }


    public static void customNotification(Project project) {
        // todo 弹窗的位置需要把侧边栏展示出来
        Notification notification = Notifications.BALLOON_LOG_GROUP
                .createNotification(
                        JsonAssistantBundle.messageOnSystem("notify.welcome.content",
                                PluginDocument.GITHUB_LINK,
                                PluginDocument.SPONSOR_LINK),
                        NotificationType.INFORMATION)
                .setTitle(JsonAssistantBundle.messageOnSystem("notify.welcome.title", JsonAssistantPlugin.getVersion()));

        notification.addAction(new QuickStartAction());

        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);

        if (window != null) {
            Balloon balloon = NotificationsManagerImpl.createBalloon(window,
                    notification,
                    false,
                    false,
                    BalloonLayoutData.fullContent(),
                    new Disposable() {
                        @Override
                        public void dispose() {
                            System.out.println("release");
                        }
                    });

            JComponent component = window.getComponent();
            balloon.show(get2(component, (BalloonImpl) balloon), Balloon.Position.above);
        }
    }

    public static RelativePoint getTarget(Component component) {
        int x = component.getWidth();
        int y = component.getHeight();

        System.out.println("x: " + x + ", y: " + y);
        return new RelativePoint(component, new Point(x, y));
    }

    public static RelativePoint get2(JComponent component, BalloonImpl balloon) {
        // 在其他平台上，气球提示显示在标题栏的右侧边缘
        // 获取分层窗格

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
        return new RelativePoint(relativeComponent, new Point(relativeComponent.getWidth(), offsetY));
    }

}
