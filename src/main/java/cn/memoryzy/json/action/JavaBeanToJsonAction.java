package cn.memoryzy.json.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.AttributeSerializationPersistentState;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Memory
 * @since 2023/11/27
 */
public class JavaBeanToJsonAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(JavaBeanToJsonAction.class);

    public JavaBeanToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.javabean.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.javabean.to.json.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }


    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        // 获取当前类
        PsiClass psiClass = JavaUtil.getPsiClass(event.getDataContext());
        // JsonMap
        Map<String, Object> jsonMap = new TreeMap<>();
        // 忽略的属性
        Map<String, List<String>> ignoreMap = new HashMap<>();
        // 相关配置
        AttributeSerializationPersistentState persistentState = AttributeSerializationPersistentState.getInstance();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(project, psiClass, jsonMap, ignoreMap, persistentState);
        } catch (Error e) {
            LOG.error(e);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("tip.json.serialize.recursion"), NotificationType.ERROR, project);
            return;
        }

        String jsonStr = JSONUtil.toJsonStr(jsonMap, JsonUtil.HUTOOL_JSON_CONFIG);

        // 添加至剪贴板
        PlatformUtil.setClipboard(JsonUtil.formatJson(jsonStr));

        Set<Map.Entry<String, List<String>>> entries = ignoreMap.entrySet();
        // 移除 value 为空列表的键值对
        entries.removeIf(entry -> entry.getValue().isEmpty());

        if (CollUtil.isNotEmpty(entries)) {
            Notifications.showFullNotification(
                    JsonAssistantBundle.messageOnSystem("tip.json.serialize.ignore.title"),
                    generateNotificationContent(entries),
                    NotificationType.INFORMATION,
                    project);
        } else {
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("tip.json.serialize.copy"), NotificationType.INFORMATION, project);
        }
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置可见性
        event.getPresentation().setEnabledAndVisible(
                Objects.nonNull(event.getProject())
                        && JavaUtil.isJavaFile(event.getDataContext())
                        && JavaUtil.hasJavaProperty(event.getDataContext()));
    }


    public static String generateNotificationContent(Set<Map.Entry<String, List<String>>> entries) {
        String notificationContent = "<ul>{}</ul>";
        String concreteContent = "<li>{}</li>";

        List<String> nameList = new ArrayList<>(entries.size());
        // 类名 key，value 字段集
        for (Map.Entry<String, List<String>> entry : entries) {
            // 类全限定名
            String key = entry.getKey();
            // 被忽略字段集合
            List<String> value = entry.getValue();

            key = StringUtil.getShortName(key);
            for (String fieldName : value) {
                nameList.add(fieldName + " (" + key + ")");
            }
        }

        // 强调倒序
        StringBuilder builder = new StringBuilder();
        for (int i = nameList.size() - 1; i >= 0; i--) {
            builder.append(StrUtil.format(concreteContent, nameList.get(i)));
        }

        return StrUtil.format(notificationContent, builder.toString());
    }

}