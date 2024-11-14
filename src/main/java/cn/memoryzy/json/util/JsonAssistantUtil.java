package cn.memoryzy.json.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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


    /**
     * 判断一个字符是否是中文字符（基本汉字范围）
     */
    private static boolean isChineseCharacter(char c) {
        return (c >= '一' && c <= '\u9FFF');
    }


    /**
     * 去除\r相关
     *
     * @param text 文本
     * @return 规范后的文本
     */
    public static String normalizeLineEndings(String text) {
        return text == null ? null : text.replaceAll("\\r\\n|\\r|\\n", "\n");
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


    public static String unicodeToString(String unicodeString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unicodeString.length(); i++) {
            if (i + 5 < unicodeString.length() && unicodeString.charAt(i) == '\\' && unicodeString.charAt(i + 1) == 'u') {
                // 判断是否是 Unicode 编码
                String hexCode = unicodeString.substring(i + 2, i + 6);
                int codePoint = Integer.parseInt(hexCode, 16);
                sb.append(Character.toChars(codePoint));
                i += 5; // 跳过 Unicode 编码
            } else {
                sb.append(unicodeString.charAt(i));
            }
        }
        return sb.toString();
    }

}
