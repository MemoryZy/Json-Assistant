package cn.memoryzy.json.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonConversionTarget;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

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
        presentation.setText(JsonAssistantBundle.message("action.serialize.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.description"));
        presentation.setIcon(JsonAssistantIcons.JSON);
    }


    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        // 在此判断当前光标是在某个对象 或 对象实例 及 List、数组上，那就将该对象 或 对象实例解析为JSON
        PsiClass psiClass = JavaUtil.getCurrentCursorPositionClass(project, dataContext);
        // 执行操作
        convertAttributesToJsonAndNotify(project, psiClass, JsonUtil::formatJson, false, LOG);
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置可见性
        Presentation presentation = event.getPresentation();
        presentation.setEnabledAndVisible(updateActionAndCheckEnablement(getEventProject(event), event.getDataContext(), presentation, false));
    }


    /**
     * 将 Java 属性转换为 JSON/JSON5 并复制到剪贴板，同时显示忽略的字段（如果有的话）
     *
     * @param project        项目对象
     * @param psiClass       当前类对象
     * @param jsonConverter  JSON 转换器
     * @param resolveComment 是否解析注释
     * @param log            日志对象
     */
    public static void convertAttributesToJsonAndNotify(Project project, PsiClass psiClass, Function<Map<String, Object>, String> jsonConverter, boolean resolveComment, Logger log) {
        // JsonMap
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        // 忽略的属性
        Map<String, List<String>> ignoreMap = new LinkedHashMap<>();
        // 最外层的注释Map
        Map<String, String> commentMap = new HashMap<>();

        // 相关配置
        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(project, psiClass, jsonMap, ignoreMap, commentMap, resolveComment, persistentState.attributeSerializationState);
        } catch (Error e) {
            log.error(e);
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("error.serialize.recursion"), NotificationType.ERROR, project);
            return;
        }

        // 执行转换
        String jsonStr = jsonConverter.apply(jsonMap);

        // 添加至剪贴板
        PlatformUtil.setClipboard(jsonStr);

        Set<Map.Entry<String, List<String>>> entries = ignoreMap.entrySet();
        // 移除 value 为空列表的键值对
        entries.removeIf(entry -> entry.getValue().isEmpty());

        if (CollUtil.isNotEmpty(entries)) {
            Notifications.showFullNotification(
                    JsonAssistantBundle.messageOnSystem("notification.serialize.ignore.title"),
                    generateNotificationContent(entries),
                    NotificationType.INFORMATION,
                    project);
        } else {
            Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notification.serialize.copy"), NotificationType.INFORMATION, project);
        }
    }


    public static boolean updateActionAndCheckEnablement(Project project, DataContext dataContext, Presentation presentation, boolean isJson5) {
        if (Objects.isNull(project)) {
            return false;
        }

        if (!JavaUtil.isJavaFile(dataContext)) {
            return false;
        }

        ImmutablePair<JsonConversionTarget, PsiClass> pair = JavaUtil.getCurrentCursorPositionClass2(project, dataContext);
        JsonConversionTarget target = pair.getLeft();
        PsiClass psiClass = pair.getRight();
        if (psiClass == null) {
            return false;
        }

        if (!JavaUtil.hasJavaProperty(psiClass)) {
            return false;
        }

        // 更新Action
        String newName = isJson5 ? target.getJson5Name() : target.getJsonName();
        String newDescription = isJson5 ? target.getJson5Description() : target.getJsonDescription();

        String oldName = presentation.getText();
        String oldDescription = presentation.getDescription();

        if (!Objects.equals(newName, oldName)) presentation.setText(newName);
        if (!Objects.equals(newDescription, oldDescription)) presentation.setDescription(newDescription);

        return true;
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