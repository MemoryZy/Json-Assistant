package cn.memoryzy.json.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.AttributeSerializationPersistentState;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Memory
 * @since 2024/7/5
 */
public class KotlinPropertyToJsonAction extends AnAction implements UpdateInBackground {

    private static final Logger LOG = Logger.getInstance(KotlinPropertyToJsonAction.class);

    public KotlinPropertyToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.kt.class.property.to.json.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.kt.class.property.to.json.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiClass currentPsiClass = KotlinUtil.getPsiClass(event.getDataContext());
        Map<String, Object> jsonMap = new TreeMap<>();
        // 忽略的属性
        Map<String, List<String>> ignoreMap = new HashMap<>();
        // 相关配置
        AttributeSerializationPersistentState persistentState = AttributeSerializationPersistentState.getInstance();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(project, currentPsiClass, jsonMap, ignoreMap, persistentState);
        } catch (Error e) {
            LOG.error(e);
            // 给通知
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("tip.json.serialize.recursion"), NotificationType.ERROR, project);
            return;
        }

        // 将Map转换为Json
        String jsonStr = JSONUtil.toJsonStr(jsonMap, JsonUtil.HUTOOL_JSON_CONFIG);
        // 添加至剪贴板
        PlatformUtil.setClipboard(JsonUtil.formatJson(jsonStr));

        Set<Map.Entry<String, List<String>>> entries = ignoreMap.entrySet();
        // 移除 value 为空列表的键值对
        entries.removeIf(entry -> entry.getValue().isEmpty());

        if (CollUtil.isNotEmpty(ignoreMap)) {
            Notifications.showFullNotification(
                    JsonAssistantBundle.messageOnSystem("tip.json.serialize.ignore.title"),
                    JavaBeanToJsonAction.generateNotificationContent(entries),
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
                        && KotlinUtil.isKtFile(event.getDataContext())
                        && KotlinUtil.hasKtProperty(event.getDataContext()));
    }
}
