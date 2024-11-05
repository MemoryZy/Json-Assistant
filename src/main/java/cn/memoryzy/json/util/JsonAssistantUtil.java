package cn.memoryzy.json.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.HtmlConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.enums.FileTypes;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class JsonAssistantUtil {

    public static String truncateText(String text, int maxLength, String omitHint) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + " " + omitHint;
        } else {
            return text;
        }
    }

    public static Class<?> getClassByName(String classQualifiedName) {
        try {
            return Class.forName(classQualifiedName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Object readStaticFinalFieldValue(Class<?> clz, String fieldName) {
        Field matchField = null;
        try {
            for (Field field : ClassUtil.getDeclaredFields(clz)) {
                if (Objects.equals(fieldName, field.getName())) {
                    // 检查字段是否是静态且Final的
                    if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        matchField = field;
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return Objects.isNull(matchField) ? null : ReflectUtil.getStaticFieldValue(matchField);
    }

    public static Method getMethod(Object obj, String methodName, Object... params) {
        Class<?> clazz = obj.getClass();
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getClass();
        }

        return ReflectUtil.getMethod(clazz, methodName, paramTypes);
    }


    @SuppressWarnings("UnusedReturnValue")
    public static Object invokeMethod(Object obj, String methodName, Object... params) {
        Method method = getMethod(obj, methodName, params);
        if (Objects.nonNull(method)) {
            return ReflectUtil.invoke(obj, method, params);
        }

        return null;
    }


    public static void openOnlineDoc(Project project, boolean useHtmlEditor) {
        String url = Urls.OVERVIEW;
        boolean darkTheme = UIUtil.isUnderDarcula();
        Map<String, String> parameters = darkTheme ? Map.of("theme", "dark") : Map.of("theme", "light");
        url = com.intellij.util.Urls.newFromEncoded(url).addParameters(parameters).toExternalForm();

        if (PlatformUtil.canBrowseInHTMLEditor() && useHtmlEditor) {
            String timeoutContent = HtmlConstant.TIMEOUT_HTML
                    .replace("__THEME__", darkTheme ? "theme-dark" : "")
                    .replace("__TITLE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.title"))
                    .replace("__MESSAGE__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.message"))
                    .replace("__ACTION__", JsonAssistantBundle.messageOnSystem("open.html.editor.timeout.action", url));

            if (Urls.isReachable()) {
                HTMLEditorProvider.openEditor(project, JsonAssistantBundle.messageOnSystem("html.editor.quick.start.title"), url, timeoutContent);
                return;
            }
        }

        BrowserUtil.browse(url);
    }

    public static boolean isJsonFileType(FileType fileType) {
        return isAssignFileType(fileType, FileTypes.JSON.getFileTypeQualifiedName())
                || isAssignFileType(fileType, FileTypes.JSON5.getFileTypeQualifiedName());
    }

    public static boolean isAssignFileType(FileType fileType, String fileTypeClassName) {
        return fileType != null && Objects.equals(fileTypeClassName, fileType.getClass().getName());
    }

    /**
     * 判断一个字符是否是中文字符（基本汉字范围）
     */
    private static boolean isChineseCharacter(char c) {
        return (c >= '一' && c <= '\u9FFF');
    }

    /**
     * 判断文本中是否存在多个中文字符
     */
    public static boolean containsMultipleChineseCharacters(String text) {
        int chineseCount = 0;
        for (char c : text.toCharArray()) {
            if (isChineseCharacter(c)) {
                chineseCount++;
                if (chineseCount > 1) {
                    // 一旦发现超过一个中文字符，立即返回true
                    return true;
                }
            }
        }

        // 遍历完所有字符后，如果没有发现超过一个中文字符，则返回false
        return false;
    }


    /**
     * 判断字符串是否属于数字、时间或布尔类型
     *
     * @param str 输入的字符串
     * @return 返回字符串转化为对应类型后的值
     */
    public static Object detectType(String str) {
        Object number = getNumber(str);
        if (Objects.nonNull(number)) return number;

        Object bool = getBoolean(str);
        if (Objects.nonNull(bool)) return bool;

        Object date = getDate(str);
        if (Objects.nonNull(date)) return date;

        return str;
    }


    public static Object getNumber(String str) {
        try {
            if (NumberUtil.isNumber(str)) {
                return NumberUtil.parseNumber(str);
            }
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    public static Object getBoolean(String str) {
        if (StrUtil.equalsIgnoreCase(Boolean.TRUE.toString(), str)) {
            return true;
        } else if (StrUtil.equalsIgnoreCase(Boolean.FALSE.toString(), str)) {
            return false;
        }

        return null;
    }

    public static Object getDate(String str) {
        try {
            return DateUtil.parse(str);
        } catch (Exception ignored) {
        }

        return null;
    }

}
