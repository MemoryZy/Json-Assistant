package cn.memoryzy.json.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonConversionTarget;
import cn.memoryzy.json.model.TypeNamePair;
import cn.memoryzy.json.service.persistent.state.v2.SerializationState;
import cn.memoryzy.json.service.persistent.v2.SerializationSettings;
import cn.memoryzy.json.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import icons.JsonAssistantIcons;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        convertAttributesToJsonAndNotify(project, dataContext, false, JsonUtil::formatJson, false, LOG);
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
     * @param jsonConverter  JSON 转换器
     * @param resolveComment 是否解析注释
     * @param log            日志对象
     */
    public static void convertAttributesToJsonAndNotify(Project project, DataContext dataContext, boolean isKt, Function<Map<String, Object>, String> jsonConverter, boolean resolveComment, Logger log) {
        PsiClass psiClass;
        JsonConversionTarget target;
        if (isKt) {
            // 获取当前类
            psiClass = KotlinUtil.getPsiClass(dataContext);
            target = JsonConversionTarget.CURRENT_CLASS;
        } else {
            // 在此判断当前光标是在某个对象 或 对象实例 及 List、数组上，那就将该对象 或 对象实例解析为JSON
            ImmutablePair<JsonConversionTarget, PsiClass> pair = JavaUtil.getCurrentCursorPositionClass2(project, dataContext);
            target = pair.getLeft();
            psiClass = pair.getRight();
        }

        // JsonMap
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        // 忽略的属性
        Map<String, List<String>> ignoreMap = new LinkedHashMap<>();
        // 最外层的注释Map
        Map<String, String> commentMap = new HashMap<>();

        // 相关配置
        SerializationState serializationState = SerializationSettings.getInstance().getSerializationState();

        // 获取忽略字段，只有当前类才进行字段筛选
        List<String> ignoredFields = getIgnoredFields(dataContext, psiClass, target);

        try {
            // 递归添加所有属性，包括嵌套属性
            JavaUtil.recursionAddProperty(project, psiClass, jsonMap, ignoreMap, ignoredFields, commentMap, resolveComment, serializationState);
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

    /**
     * 筛选出不进行序列化的字段
     *
     * @return 不进行序列化的字段列表，格式为：类限定名.字段
     */
    private static List<String> getIgnoredFields(DataContext dataContext, PsiClass psiClass, JsonConversionTarget target) {
        List<String> ignoredFields = new ArrayList<>();
        if (JsonConversionTarget.CURRENT_CLASS != target) return ignoredFields;

        // 选中文本
        String selectText = PlatformUtil.getSingleSelectText(PlatformUtil.getEditor(dataContext));
        if (StrUtil.isBlank(selectText)) return ignoredFields;

        // 解析选中文本中的字段
        List<String> lines = StrUtil.split(selectText, "\n", true, true);
        List<TypeNamePair> pairs = lines.stream()
                .map(JavaBeanToJsonAction::parseNonStaticField)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(pairs)) return ignoredFields;

        String qualifiedName = psiClass.getQualifiedName();
        PsiField[] fields = JavaUtil.getNonStaticFields(psiClass);
        for (PsiField field : fields) {
            String fieldName = field.getName();
            PsiType fieldType = field.getType();
            String presentableText = fieldType.getPresentableText();

            // 判断内部类
            PsiClass typeClass = PsiTypesUtil.getPsiClass(fieldType);
            if (null != typeClass) {
                PsiClass containingClass = typeClass.getContainingClass();
                if (null != containingClass) {
                    presentableText = containingClass.getName() + "." + presentableText;
                }
            }

            // 类型、名称匹配
            String finalPresentableText = presentableText;
            if (pairs.stream().noneMatch(el -> Objects.equals(finalPresentableText, el.getType()) && Objects.equals(fieldName, el.getName()))) {
                // 匹配失败的就加入忽略字段中
                ignoredFields.add(StrUtil.format("{}.{}", qualifiedName, fieldName));
            }
        }

        return ignoredFields;
    }

    /**
     * 解析非静态字段声明，返回字段类型和名称
     *
     * @param declaration 字段声明字符串（如 "private String name;"）
     * @return 对象[类型, 名称]，若为静态字段或格式错误则返回null
     */
    public static TypeNamePair parseNonStaticField(String declaration) {
        // 正则表达式分解字段声明
        String regex =
                "^\\s*" +                                  // 起始空格
                        "(?:(?:@\\w+\\s+)+)?" +                    // 注解（如 @Autowired）
                        "\\b(?:(public|protected|private|final|transient|volatile)\\s+)*\\b" + // 修饰符
                        "(?!.*\\bstatic\\b)" +                     // 排除包含static的字段
                        "([\\w<>$,.]+)\\s+" +                 // 类型
                        "(\\w+)\\s*" +                             // 字段名
                        "(?:=.*?)?\\s*;\\s*$";                     // 忽略初始化部分

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(declaration.trim());

        if (matcher.find()) {
            String type = StrUtil.trim(matcher.group(2));
            String name = StrUtil.trim(matcher.group(3));
            return new TypeNamePair(type, name);
        } else {
            String arrayRegex =
                    "^\\s*" +
                            "(?!.*\\bstatic\\b)" +              // 排除 static
                            "(?:[\\w.$]+\\s+)*" +               // 修饰符（如 public、private）
                            "([a-zA-Z_$][\\w.$]*(?:\\s*\\[\\s*]\\s*)+)" +  // 类型（带数组）
                            "\\s+" +
                            "(\\w+)" +                          // 字段名
                            "\\s*;" +                           // 以分号结尾
                            "$";

            Pattern arrayPattern = Pattern.compile(arrayRegex, Pattern.MULTILINE);
            Matcher arrayMatcher = arrayPattern.matcher(declaration.trim());
            if (arrayMatcher.find()) {
                String type = StrUtil.trim(arrayMatcher.group(1));
                String name = StrUtil.trim(arrayMatcher.group(2));
                return new TypeNamePair(type, name);
            }
        }

        return null;
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