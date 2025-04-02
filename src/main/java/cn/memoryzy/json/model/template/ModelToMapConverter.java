package cn.memoryzy.json.model.template;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Memory
 * @since 2025/4/2
 */
public class ModelToMapConverter {

    public static Map<String, Object> convert(Object model) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        Class<?> clz = model.getClass();
        for (Field field : clz.getDeclaredFields()) {
            MapKey annotation = field.getAnnotation(MapKey.class);
            if (annotation == null) continue;

            ReflectUtil.setAccessible(field);
            Object value = field.get(model);

            result.put(annotation.value(), processValue(value));
        }
        return result;
    }

    private static Object processValue(Object value) throws Exception {
        if (value == null) return null;

        // 处理集合类型
        if (value instanceof Collection) {
            return processCollection((Collection<?>) value);
        }

        // 处理数组类型
        if (value.getClass().isArray()) {
            return processArray(value);
        }

        // 处理嵌套模型
        if (value instanceof TemplateModel) {
            return convert(value);
        }

        // 基本类型直接返回
        return value;
    }

    private static List<Object> processCollection(Collection<?> collection) throws Exception {
        List<Object> list = new ArrayList<>();
        for (Object item : collection) {
            list.add(processCollectionItem(item));
        }
        return list;
    }

    private static List<Object> processArray(Object array) throws Exception {
        List<Object> list = new ArrayList<>();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object item = Array.get(array, i);
            list.add(processCollectionItem(item));
        }
        return list;
    }

    private static Object processCollectionItem(Object item) throws Exception {
        return (item instanceof TemplateModel) ? convert(item) : item;
    }

}