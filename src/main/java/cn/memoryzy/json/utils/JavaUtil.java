package cn.memoryzy.json.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.JsonAnnotationEnum;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Memory
 * @since 2024/7/3
 */
public class JavaUtil {

    /**
     * 递归将属性转成Map元素
     *
     * @param psiClass        class
     * @param jsonMap         Map
     * @param ignoreFieldList
     */
    public static void recursionAddProperty(PsiClass psiClass, Map<String, Object> jsonMap, List<String> ignoreFieldList) {
        // 获取该类所有字段
        PsiField[] allFields = JavaUtil.getAllFieldFilterStatic(psiClass);
        for (PsiField psiField : allFields) {
            String fieldName = psiField.getName();

            // -------------------------- 注解支持
            // 获取Json键名
            String jsonKeyName = getAnnotationJsonKeyName(psiField);

            // 如果加了忽略，则忽略该属性；或属性为临时属性，也忽略
            if (Objects.equals(PluginConstant.PLUGIN_ID, jsonKeyName)
                    || psiField.hasModifierProperty(PsiModifier.TRANSIENT)
                    || psiField.hasAnnotation(PluginConstant.KOTLIN_TRANSIENT)) {
                ignoreFieldList.add(fieldName);
                continue;
            }

            // 字段名
            String propertyName = (StrUtil.isBlank(jsonKeyName)) ? fieldName : jsonKeyName;

            // 字段类型
            PsiType psiType = psiField.getType();

            // 是否为引用类型
            if (JavaUtil.isReferenceType(psiType)) {
                // 嵌套Map（为了实现嵌套属性）
                Map<String, Object> nestedJsonMap = new HashMap<>();
                // 递归
                recursionAddProperty(JavaUtil.getPsiClassByReferenceType(psiType), nestedJsonMap, ignoreFieldList);
                // 添加至主Map
                jsonMap.put(propertyName, nestedJsonMap);
            } else {
                // key，名称；value，根据全限定名判断生成具体的内容
                jsonMap.put(propertyName, getDefaultValue(psiType.getCanonicalText()));
            }
        }
    }


    /**
     * 获取Json注解中的键名称
     *
     * @param psiField 字段属性
     * @return 键名（如果是{@link PluginConstant#PLUGIN_ID}）则表示忽略该字段
     */
    private static String getAnnotationJsonKeyName(PsiField psiField) {
        // ---------------------------------- 获取注解判断是否忽略序列化
        // jackson 通过 @JsonIgnore 注解标记是否忽略序列化字段
        PsiAnnotation jacksonIgnore = psiField.getAnnotation(JsonAnnotationEnum.JACKSON_JSON_IGNORE.getValue());
        if (Objects.nonNull(jacksonIgnore)) {
            // 该字段需要忽略
            return PluginConstant.PLUGIN_ID;
        }

        // 检测是否含有 fastjson 注解
        PsiAnnotation fastJsonJsonField = psiField.getAnnotation(JsonAnnotationEnum.FAST_JSON_JSON_FIELD.getValue());
        PsiAnnotation fastJson2JsonField = psiField.getAnnotation(JsonAnnotationEnum.FAST_JSON2_JSON_FIELD.getValue());
        // 检测是否含有 Jackson 注解
        PsiAnnotation jacksonJsonProperty = psiField.getAnnotation(JsonAnnotationEnum.JACKSON_JSON_PROPERTY.getValue());

        String annotationValue = "";
        if (Objects.nonNull(fastJsonJsonField) || Objects.nonNull(fastJson2JsonField)) {
            // 是否忽略序列化
            String serialize = JavaUtil.getMemberValue(fastJsonJsonField, "serialize");
            String serialize2 = JavaUtil.getMemberValue(fastJson2JsonField, "serialize");
            if (Objects.equals(Boolean.FALSE.toString(), serialize) || Objects.equals(Boolean.FALSE.toString(), serialize2)) {
                return PluginConstant.PLUGIN_ID;
            }

            // 获取值
            annotationValue = JavaUtil.getMemberValue(fastJsonJsonField, "name");
            if (StrUtil.isBlank(annotationValue)) {
                annotationValue = JavaUtil.getMemberValue(fastJson2JsonField, "name");
            }

        } else if (Objects.nonNull(jacksonJsonProperty)) {
            annotationValue = JavaUtil.getMemberValue(jacksonJsonProperty, "value");
        }

        return annotationValue;
    }


    /**
     * 根据类全限定名获取其类型的默认值
     *
     * @param canonicalText 全限定名
     * @return 默认值
     */
    public static Object getDefaultValue(String canonicalText) {
        Object result;

        if ("boolean".equals(canonicalText) || "java.lang.Boolean".equals(canonicalText)) {
            result = false;
        } else if ("char".equals(canonicalText) || "java.lang.Character".equals(canonicalText) || "java.lang.String".equals(canonicalText)) {
            result = "";
        } else if ("byte".equals(canonicalText) || "java.lang.Byte".equals(canonicalText)
                || "short".equals(canonicalText) || "java.lang.Short".equals(canonicalText)
                || "int".equals(canonicalText) || "java.lang.Integer".equals(canonicalText)
                || "long".equals(canonicalText) || "java.lang.Long".equals(canonicalText)) {
            result = 0;
        } else if ("float".equals(canonicalText) || "java.lang.Float".equals(canonicalText)
                || "double".equals(canonicalText) || "java.lang.Double".equals(canonicalText)
                || "java.math.BigDecimal".equals(canonicalText)) {
            result = new BigDecimal("1.0");
        } else if (canonicalText.contains("Date")
                || canonicalText.contains("DateTime")
                || canonicalText.contains("Timestamp")) {
            result = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);
        } else if (canonicalText.contains("List")
                || canonicalText.contains("ArrayList")
                || canonicalText.contains("Collection")) {
            result = new ArrayList<>();
        } else {
            result = null;
        }

        return result;
    }

    /**
     * 判断是否为引用类型（自己项目中的类）（不包括String、BigDecimal之类的）
     *
     * @param psiType 类型
     * @return true，引用类型；false，不为引用类型
     */
    public static boolean isReferenceType(PsiType psiType) {
        // 不为引用类型
        if (!(psiType instanceof PsiClassReferenceType)) {
            return false;
        }

        // 全限定名（基本类型就只有基本类型名 long、int）
        String canonicalText = psiType.getCanonicalText();
        // 判断是否为java包其他类
        return !StrUtil.startWith(canonicalText, "java.");
    }

    /**
     * 用两种方法获取PsiClass
     *
     * @param event 事件源
     * @return Class
     */
    public static PsiClass getPsiClass(AnActionEvent event) {
        PsiClass psiClass = null;

        try {
            // 一个类中可能存在几个内部类
            PsiClass[] psiClasses = getAllPsiClassByPsiFile(event);

            if (ArrayUtil.isEmpty(psiClasses)) {
                return getCurrentPsiClassByOffset(event);
            } else {
                // 单独Class
                if (psiClasses.length == 1) {
                    psiClass = psiClasses[0];
                } else {
                    // 偏移量获取
                    PsiClass curClz = getCurrentPsiClassByOffset(event);
                    if (Objects.nonNull(curClz)) {
                        psiClass = curClz;
                    } else {
                        psiClass = psiClasses[0];
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return psiClass;
    }


    /**
     * 根据Java文件获取当前Class文件及所有内部类
     *
     * @param event 事件信息
     * @return Class
     */
    public static PsiClass[] getAllPsiClassByPsiFile(AnActionEvent event) {
        List<PsiClass> psiClassList = new ArrayList<>();
        PsiFile psiFile = PlatformUtil.getPsiFile(event);

        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass[] classes = psiJavaFile.getClasses();
            for (PsiClass psiClass : classes) {
                psiClassList.add(psiClass);
                PsiClass[] innerClasses = psiClass.getInnerClasses();
                if (ArrayUtil.isNotEmpty(innerClasses)) {
                    psiClassList.addAll(Arrays.asList(innerClasses));
                }
            }
        }

        return psiClassList.toArray(new PsiClass[0]);
    }

    /**
     * 根据编辑器的偏移量获取当前所在的Class文件（因为利用了编辑器，光标必须在类中，也就是类的上下作用域内）
     *
     * @param event 事件信息
     * @return class
     */
    public static PsiClass getCurrentPsiClassByOffset(AnActionEvent event) {
        PsiFile psiFile = PlatformUtil.getPsiFile(event);
        Editor editor = PlatformUtil.getEditor(event);

        if (Objects.nonNull(psiFile) && (psiFile.getFileType() instanceof JavaFileType)) {
            return PsiTreeUtil.getParentOfType(PlatformUtil.getPsiElementByOffset(editor, psiFile), PsiClass.class);
        }

        return null;
    }

    /**
     * 是否存在某个依赖
     *
     * @param module      module
     * @param libraryName 依赖名，例如: org.projectlombok:lombok
     * @return true，存在；false，不存在
     */
    public static boolean hasLibrary(Module module, String libraryName) {
        final Ref<Library> result = Ref.create(null);
        OrderEnumerator.orderEntries(module).forEachLibrary(library -> {
            String name = library.getName();
            if (StrUtil.isNotBlank(name) && name.contains(libraryName)) {
                result.set(library);
                return false;
            }
            return true;
        });

        return Objects.nonNull(result.get());
    }

    /**
     * 是否处于Java文件中
     *
     * @param event 事件源
     * @return true -> 处于；false -> 不处于
     */
    public static boolean isJavaFile(AnActionEvent event) {
        // 获取当前选中的 PsiClass
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        return psiFile instanceof PsiJavaFile;
    }

    /**
     * 当前 Java 类中是否存在属性
     *
     * @param event 事件源
     * @return true -> 存在；false -> 不存在
     */
    public static boolean hasJavaProperty(AnActionEvent event) {
        boolean enabled = false;
        PsiClass psiClass = getPsiClass(event);
        if (Objects.nonNull(psiClass)) {
            PsiField[] fields = getAllFieldFilterStatic(psiClass);
            enabled = ArrayUtil.isNotEmpty(fields);
        }

        return enabled;
    }


    /**
     * 通过引用类型获取Class
     *
     * @param psiType 引用类型，必须是类引用类型{@link PsiClassReferenceType}
     * @return Class
     */
    public static PsiClass getPsiClassByReferenceType(PsiType psiType) {
        if (psiType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiType;
            return psiClassReferenceType.resolve();
        }

        return null;
    }

    /**
     * 获取该类的所有字段
     *
     * @param psiClass class
     * @return 所有字段
     */
    public static PsiField[] getAllFieldFilterStatic(PsiClass psiClass) {
        return (Objects.isNull(psiClass))
                ? new PsiField[0]
                : Arrays.stream(psiClass.getAllFields()).filter(el -> !el.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiField[]::new);
    }

    /**
     * 获取注解中的指定属性（去除"之后）
     *
     * @param psiAnnotation 注解
     * @param attributeName 属性名
     * @return 属性值
     */
    public static String getMemberValue(PsiAnnotation psiAnnotation, String attributeName) {
        String value = "";
        if (Objects.isNull(psiAnnotation)) {
            return value;
        }

        // 获取注解的属性
        PsiAnnotationMemberValue memberValue = psiAnnotation.findAttributeValue(attributeName);
        if (Objects.isNull(memberValue)) {
            return value;
        }

        // value属性值
        value = memberValue.getText();
        if (StringUtils.isNotBlank(value)) {
            value = value.replace("\"", "");
        }

        return value;
    }


    /**
     * 将指定类的引用导入给定的PsiClass中。
     *
     * @param project           Java项目
     * @param psiClass          要导入引用的PsiClass
     * @param refQualifiedNames 要导入的类的完全限定名
     */
    public static void importClassesInClass(Project project, PsiClass psiClass, String... refQualifiedNames) {
        if (Objects.isNull(project) || Objects.isNull(psiClass) || ArrayUtil.isEmpty(refQualifiedNames)) {
            return;
        }

        List<PsiClass> refClasses = new ArrayList<>(refQualifiedNames.length);
        JavaCodeStyleManager instance = JavaCodeStyleManager.getInstance(project);
        PsiJavaFile containingFile = (PsiJavaFile) psiClass.getContainingFile();
        for (String refQualifiedName : refQualifiedNames) {
            PsiClass refClass = findClass(project, refQualifiedName);
            if (Objects.nonNull(refClass)) {
                refClasses.add(refClass);
            }
        }

        if (Objects.isNull(containingFile) || CollUtil.isEmpty(refClasses)) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiClass refClass : refClasses) {
                instance.addImport(containingFile, refClass);
            }
        });
    }

    /**
     * 根据对象获取其类型字符串
     *
     * @param obj 对象
     * @return 类型名
     */
    @SuppressWarnings("rawtypes")
    public static String getStrType(Object obj) {
        String type = Object.class.getSimpleName();
        if ((obj instanceof Double) || (obj instanceof Integer) || (obj instanceof Boolean)) {
            type = obj.getClass().getSimpleName();

        } else if (obj instanceof String) {
            String str = (String) obj;
            // 时间类型判断
            return checkJsonDateType(str);

        } else if (obj instanceof List) {
            List list = (List) obj;
            // 判断是否有值
            if (list.isEmpty()) {
                type = List.class.getSimpleName();
            } else {
                String genericsType = getStrType(list.get(0));
                type = StrUtil.format("{}<{}>", List.class.getSimpleName(), genericsType);
            }
        }

        return type;
    }


    /**
     * 判断是否为时间类型
     *
     * @param obj 参数
     * @return 为时间类型返回Date、否则返回String
     */
    private static String checkJsonDateType(String obj) {
        String type = String.class.getSimpleName();
        try {
            DateTime time = DateUtil.parse(obj);
            if (Objects.nonNull(time)) {
                type = Date.class.getSimpleName();
            }
        } catch (DateException e) {
            // 忽略异常
        }
        return type;
    }

    /**
     * 查找指定项目中的类。
     *
     * @param project       项目对象
     * @param qualifiedName 类的全限定名
     * @return 匹配的PsiClass对象，如果没有找到则返回null
     */
    public static PsiClass findClass(Project project, String qualifiedName) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        return psiFacade.findClass(qualifiedName, GlobalSearchScope.allScope(project));
    }


}
