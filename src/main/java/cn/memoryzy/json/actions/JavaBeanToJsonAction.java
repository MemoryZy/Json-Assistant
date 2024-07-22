package cn.memoryzy.json.actions;

import cn.hutool.core.collection.CollUtil;
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
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

        if (PlatformUtil.isNewUiVersion()) {
            presentation.setIcon(JsonAssistantIcons.NEW_JSON);
        } else {
            presentation.setIcon(JsonAssistantIcons.JSON);
        }
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
        // 忽略的属性
        Map<String, List<String>> ignoreMap = new HashMap<>();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(project, psiClass, jsonMap, ignoreMap);
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

        Set<Map.Entry<String, List<String>>> entries = ignoreMap.entrySet();
        // 移除 value 为空列表的键值对
        entries.removeIf(entry -> entry.getValue().isEmpty());

        if (CollUtil.isNotEmpty(entries)) {
            String ignoreHtml = createNotifyIgnoreHtml(entries);
            Notification.notifyLog(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.ignore", ignoreHtml), NotificationType.INFORMATION, project);
        }

        // 给通知
        Notification.notify(JsonAssistantBundle.messageOnSystem("notify.javabean.to.json.tip.copy"), NotificationType.INFORMATION, project);
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置可见性
        event.getPresentation().setEnabledAndVisible(Objects.nonNull(event.getProject()) && JavaUtil.isJavaFile(event) && JavaUtil.hasJavaProperty(event));
    }


    public static String createNotifyIgnoreHtml(Set<Map.Entry<String, List<String>>> entries) {
        StringBuilder builder = new StringBuilder("<br/>");
        if (entries.size() == 1) {
            builder.append("<ul>");
            // 类名 key，value 字段集
            for (Map.Entry<String, List<String>> entry : entries) {
                // 类全限定名
                String key = entry.getKey();
                // 被忽略字段集合
                List<String> value = entry.getValue();

                for (String fieldName : value) {
                    builder.append("<li>").append(fieldName).append("</li>");
                }
            }

            builder.append("</ul>");
        } else {
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

            builder.append("<ul>");
            for (int i = nameList.size() - 1; i >= 0; i--) {
                builder.append("<li>").append(nameList.get(i)).append("</li>");
            }

            builder.append("</ul>");
        }

        return builder.toString();
    }

}