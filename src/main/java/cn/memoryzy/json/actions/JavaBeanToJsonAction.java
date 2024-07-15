package cn.memoryzy.json.actions;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.JavaUtil;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.Notification;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Memory
 * @since 2023/11/27
 */
public class JavaBeanToJsonAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(JavaBeanToJsonAction.class);

    public JavaBeanToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.javabean.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.javabean.to.json.description"));
    }


    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        // 获取当前类
        PsiClass psiClass = JavaUtil.getPsiClass(event);
        // JsonMap
        Map<String, Object> jsonMap = new TreeMap<>();
        List<String> ignoreFieldList = new ArrayList<>();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(psiClass, jsonMap, ignoreFieldList);
        } catch (Error e) {
            LOG.error(e);
            // 给通知
            Notification.notify(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.recursion"), NotificationType.ERROR, project);
            return;
        }

        String jsonStr = JSONUtil.toJsonStr(jsonMap, JSONConfig.create().setStripTrailingZeros(false));
        jsonStr = JsonUtil.formatJson(jsonStr);

        // 添加至剪贴板
        PlatformUtil.setClipboard(jsonStr);

        if (CollUtil.isNotEmpty(ignoreFieldList)) {
            String ignoreStr = StrUtil.join(" , ", ignoreFieldList);
            Notification.notifyLog(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.ignore", ignoreStr), NotificationType.INFORMATION, project);
        }

        // 给通知
        Notification.notify(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.copy"), NotificationType.INFORMATION, project);
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置可见性
        event.getPresentation().setEnabledAndVisible(Objects.nonNull(event.getProject()) && JavaUtil.isJavaFile(event) && JavaUtil.hasJavaProperty(event));
    }

}
