package cn.memoryzy.json.utils;

import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class Balloons {

    public static void showBalloon(Project project){
        IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);


    }

}
