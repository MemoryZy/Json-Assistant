package cn.memoryzy.json.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class JsonAssistantUtil {

    private static final long MIN_VALID_TIMESTAMP_SECONDS = 0L; // 1970-01-01T00:00:00Z
    private static final long MAX_VALID_TIMESTAMP_SECONDS = 4102444800L; // 2099-12-31T23:59:59Z

    private static final long MIN_VALID_TIMESTAMP_MILLIS = MIN_VALID_TIMESTAMP_SECONDS * 1000;
    private static final long MAX_VALID_TIMESTAMP_MILLIS = MAX_VALID_TIMESTAMP_SECONDS * 1000;

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

    public static Date getDate(String str) {
        try {
            return DateUtil.parse(str);
        } catch (Exception ignored) {
        }

        return null;
    }

    public static Date getDateAndFilterTime(String str) {
        try {
            if (NumberUtil.isNumber(str)) {
                return null;
            }

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

    public static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        List<T> result = new ArrayList<>();
        if (Objects.isNull(enumeration)) {
            return result;
        }

        while (enumeration.hasMoreElements()) {
            result.add(enumeration.nextElement());
        }

        return result;
    }

    public static boolean hasStringType(Object... array) {
        return ArrayUtil.isNotEmpty(array) && Arrays.stream(array).anyMatch(String.class::isInstance);
    }

    public static boolean allElementsAreNumeric(Object... array) {
        return ArrayUtil.isNotEmpty(array) && Arrays.stream(array)
                .allMatch(el -> el instanceof Byte || el instanceof Short || el instanceof Integer
                        || el instanceof Long || el instanceof Float
                        || el instanceof Double || el instanceof Character);
    }

    /**
     * 下划线、空格转驼峰
     *
     * @param text 文本
     * @return 转换后的文本
     */
    public static String toCamel(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        String[] split = text.split(" ");
        if (split.length > 1) {
            text = blankToSnakeCase(text);
        }

        return NamingCase.toCamelCase(text);
    }

    /**
     * 空白分隔转下划线
     *
     * @param text 文本
     * @return 转换后的文本
     */
    public static String blankToSnakeCase(String text) {
        // 替换开头的空格
        text = text.replaceAll("^\\s+", "_");
        // 替换结尾的空格
        text = text.replaceAll("\\s+$", "_");
        // 替换所有空格
        text = text.replaceAll("\\s+", "_");
        return text;
    }


    /**
     * 判断对象是否继承自某个类
     *
     * @param obj                对象
     * @param fullyQualifiedName 类的完全限定名
     * @return 是否继承自指定类
     */
    public static boolean isInheritedFrom(Object obj, String fullyQualifiedName) {
        if (obj == null || StrUtil.isBlank(fullyQualifiedName)) {
            return false;
        }

        Class<?> currentClass = obj.getClass();
        while (currentClass != null) {
            if (fullyQualifiedName.equals(currentClass.getName())) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }


    /**
     * 根据时间戳的详细信息（时、分、秒、毫秒）选择合适的日期时间格式并返回格式化后的字符串。
     *
     * @param timestamp 时间戳（单位：毫秒）
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateBasedOnTimestampDetails(long timestamp) {
        String timestampStr = timestamp + "";
        if (timestampStr.length() == 10) { // Seconds-based timestamp
            timestamp = timestamp * 1000;
        }

        LocalDateTime localDateTime = LocalDateTimeUtil.of(timestamp);

        int hour = localDateTime.getHour();
        int minute = localDateTime.getMinute();
        int second = localDateTime.getSecond();
        // 获取毫秒部分
        int millis = (int) ((timestamp % 1000));

        String format;
        if (hour == 0 && minute == 0 && second == 0 && millis == 0) {
            // 如果时、分、秒和毫秒都为0，则只显示日期
            format = DatePattern.NORM_DATE_PATTERN;
        } else if (millis > 0) {
            // 如果毫秒部分存在值，则包含毫秒的格式化表达式
            format = DatePattern.NORM_DATETIME_MS_PATTERN;
        } else {
            // 当秒为0时也使用 NORM_DATETIME_PATTERN 格式化时间
            format = DatePattern.NORM_DATETIME_PATTERN;
        }

        return LocalDateTimeUtil.format(localDateTime, format);
    }

    /**
     * 验证是否为合法的时间戳格式
     *
     * @param timestampStr 时间戳字符串
     * @return 合法的时间戳为true，否则为false
     */
    public static boolean isValidTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);

            if (timestampStr.length() == 10) { // Seconds-based timestamp
                return timestamp >= MIN_VALID_TIMESTAMP_SECONDS && timestamp <= MAX_VALID_TIMESTAMP_SECONDS;
            } else if (timestampStr.length() == 13) { // Milliseconds-based timestamp
                return timestamp >= MIN_VALID_TIMESTAMP_MILLIS && timestamp <= MAX_VALID_TIMESTAMP_MILLIS;
            } else {
                return false; // Invalid length for a timestamp
            }
        } catch (NumberFormatException e) {
            return false; // Not a valid number
        }
    }

    public static BigDecimal parseNumber(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return new BigDecimal("0");
        }
    }

}
