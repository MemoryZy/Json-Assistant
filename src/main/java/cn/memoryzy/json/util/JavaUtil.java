package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.*;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.JsonAnnotations;
import cn.memoryzy.json.service.persistent.state.AttributeSerializationState;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
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
import com.intellij.psi.impl.source.PsiImmediateClassType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
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
     * @param ignoreMap       忽略元素列表
     * @param persistentState 持久化配置
     */
    public static void recursionAddProperty(Project project, PsiClass psiClass, Map<String, Object> jsonMap,
                                            Map<String, List<String>> ignoreMap, AttributeSerializationState persistentState) {
        // 获取该类所有字段
        PsiField[] allFields = JavaUtil.getNonStaticFields(psiClass);
        List<String> fieldNameList = new ArrayList<>();
        ignoreMap.put(psiClass.getQualifiedName(), fieldNameList);

        for (PsiField psiField : allFields) {
            String fieldName = psiField.getName();

            // -------------------------- 注解支持
            // 获取Json键名
            String jsonKeyName = getAnnotationJsonKeyName(psiField, persistentState);

            // 如果加了忽略，则忽略该属性；或属性为临时属性，也忽略
            if (Objects.equals(JsonAssistantPlugin.PLUGIN_ID_NAME, jsonKeyName)
                    || psiField.hasModifierProperty(PsiModifier.TRANSIENT)
                    || psiField.hasAnnotation(PluginConstant.KOTLIN_TRANSIENT)) {

                fieldNameList.add(fieldName);
                continue;
            }

            // 字段名
            String propertyName = (StrUtil.isBlank(jsonKeyName)) ? fieldName : jsonKeyName;

            // 字段类型
            PsiType psiType = psiField.getType();

            // 是否为引用类型
            if (JavaUtil.isApplicationClsType(psiType)) {
                // 获取类型对应的Class
                PsiClass fieldClz = PsiTypesUtil.getPsiClass(psiType);

                if (Objects.nonNull(fieldClz)) {
                    if (fieldClz.isEnum()) {
                        // 先获取常量值，没有的话，从枚举中取第一个的String类型
                        Object value = PsiUtil.computeConstantExpression(psiField);
                        if (Objects.isNull(value)) {
                            // 枚举常量在枚举类中表现为静态常量
                            PsiField[] staticFields = getStaticFields(fieldClz);
                            if (ArrayUtil.isNotEmpty(staticFields)) {
                                value = staticFields[0].getName();
                            }
                        }

                        jsonMap.put(propertyName, value);

                    } else {
                        // 嵌套Map（为了实现嵌套属性）
                        Map<String, Object> nestedJsonMap;
                        // 判断属性中是否存在本类或之前类的类型的嵌套
                        if (ignoreMap.containsKey(fieldClz.getQualifiedName())) {
                            nestedJsonMap = null;
                        } else {
                            nestedJsonMap = new LinkedHashMap<>();
                            // 递归
                            recursionAddProperty(project, fieldClz, nestedJsonMap, ignoreMap, persistentState);
                        }
                        // 添加至主Map
                        jsonMap.put(propertyName, nestedJsonMap);
                    }
                } else {
                    jsonMap.put(propertyName, null);
                }

            } else if (isCollectionOrArray(psiType)) {
                PsiClass psiClz = getGenericTypeOfCollection(project, psiType);
                ArrayList<Object> list = new ArrayList<>();
                if (psiClz != null) {
                    PsiClassType classType = PsiTypesUtil.getClassType(psiClz);
                    if (JavaUtil.isApplicationClsType(classType)) {
                        // 判断属性中是否存在本类或之前类的类型的嵌套
                        if (!ignoreMap.containsKey(psiClz.getQualifiedName())) {
                            Map<String, Object> nestedJsonMap = new LinkedHashMap<>();
                            // 递归
                            recursionAddProperty(project, psiClz, nestedJsonMap, ignoreMap, persistentState);
                            // 添加至list
                            list.add(nestedJsonMap);
                        }
                    } else {
                        Object defaultValue = getDefaultValue(psiField, classType, persistentState.includeRandomValues);
                        if (Objects.nonNull(defaultValue)) {
                            list.add(defaultValue);
                        }
                    }
                }

                jsonMap.put(propertyName, list);
            } else {
                // key，名称；value，根据全限定名判断生成具体的内容
                jsonMap.put(propertyName, getDefaultValueWithAnnotation(psiField, psiType, persistentState));
            }
        }
    }

    private static Object getDefaultValueWithAnnotation(PsiField psiField, PsiType psiType, AttributeSerializationState persistentState) {
        // 如果是加了时间序列化注解，但是类型不属于时间相关类型，那注解不生效
        boolean recognitionJacksonAnnotation = persistentState.recognitionJacksonAnnotation;
        boolean recognitionFastJsonAnnotation = persistentState.recognitionFastJsonAnnotation;

        // 因为 @JsonFormat 是独立注解，如果存在，则直接返回时间类型
        if (recognitionJacksonAnnotation) {
            PsiAnnotation jacksonJsonPropertyAnnotation = psiField.getAnnotation(JsonAnnotations.JACKSON_JSON_PROPERTY.getValue());
            PsiAnnotation jacksonFormatAnnotation = psiField.getAnnotation(JsonAnnotations.JACKSON_JSON_FORMAT.getValue());

            if (Objects.nonNull(jacksonFormatAnnotation)) {
                // 获取 @JsonFormat 中的格式
                String format = StrUtil.trim(getMemberValue(jacksonFormatAnnotation, "pattern"));
                if (StrUtil.isNotBlank(format)) {
                    try {
                        String formatted = DateUtil.format(new Date(), format);
                        // 防止 pattern 中出现 纯数字的情况
                        if (!Objects.equals(format, formatted)) {
                            return formatted;
                        }
                    } catch (Exception ignored) {
                    }
                }

                return DateUtil.now();
            }

            if (Objects.nonNull(jacksonJsonPropertyAnnotation)) {
                // 获取 @JsonProperty 中的默认值
                String defaultValue = StrUtil.trim(getMemberValue(jacksonJsonPropertyAnnotation, "defaultValue"));
                if (StrUtil.isNotBlank(defaultValue)) {
                    return defaultValue;
                }
            }
        }

        // 而 @JsonField 是集成注解，依靠属性分开功能
        if (recognitionFastJsonAnnotation) {
            PsiAnnotation fastJsonFieldAnnotation = psiField.getAnnotation(JsonAnnotations.FAST_JSON_JSON_FIELD.getValue());
            PsiAnnotation fastJsonField2Annotation = psiField.getAnnotation(JsonAnnotations.FAST_JSON2_JSON_FIELD.getValue());

            if (Objects.nonNull(fastJsonField2Annotation)) {
                Object result = resolveFastJsonAnnotation(fastJsonField2Annotation);
                if (Objects.nonNull(result)) {
                    return result;
                }
            }

            if (Objects.nonNull(fastJsonFieldAnnotation)) {
                Object result = resolveFastJsonAnnotation(fastJsonFieldAnnotation);
                if (Objects.nonNull(result)) {
                    return result;
                }
            }
        }

        return getDefaultValue(psiField, psiType, persistentState.includeRandomValues);
    }


    public static Object resolveFastJsonAnnotation(PsiAnnotation psiAnnotation) {
        // 获取 @JsonField 中的格式
        String format = StrUtil.trim(getMemberValue(psiAnnotation, "format"));
        // format不为空，表示声明了属性
        if (StrUtil.isNotBlank(format)) {
            try {
                String formatted = DateUtil.format(new Date(), format);
                // 防止 pattern 中出现 纯数字的情况
                if (!Objects.equals(format, formatted)) {
                    return formatted;
                }
            } catch (Exception ignored) {
            }

            return DateUtil.now();
        }

        // defaultValue
        String defaultValue = getMemberValue(psiAnnotation, "defaultValue");
        if (StrUtil.isNotBlank(defaultValue)) {
            return defaultValue;
        }

        return null;
    }

    public static boolean isCollectionOrArray(PsiType psiType) {
        return isAssignType(psiType, PluginConstant.COLLECTION_FQN) || psiType instanceof PsiArrayType;
    }

    public static PsiClass getGenericTypeOfCollection(Project project, PsiType psiType) {
        String typeClassName;
        String canonicalText = psiType.getCanonicalText();
        if (psiType instanceof PsiArrayType) {
            typeClassName = canonicalText.replace("[]", "");
        } else {
            typeClassName = ReUtil.get("<(.*?)>", canonicalText, 1);
        }

        return findClass(project, typeClassName);
    }

    /**
     * 获取Json注解中的键名称
     *
     * @param psiField        字段属性
     * @param persistentState 持久化配置
     * @return 键名（如果是{@link JsonAssistantPlugin#PLUGIN_ID_NAME}）则表示忽略该字段
     */
    private static String getAnnotationJsonKeyName(PsiField psiField, AttributeSerializationState persistentState) {
        // ---------------------------------- 获取注解判断是否忽略序列化
        boolean recognitionFastJsonAnnotation = persistentState.recognitionFastJsonAnnotation;
        boolean recognitionJacksonAnnotation = persistentState.recognitionJacksonAnnotation;

        // jackson 通过 @JsonIgnore 注解标记是否忽略序列化字段
        if (recognitionJacksonAnnotation) {
            PsiAnnotation jacksonIgnore = psiField.getAnnotation(JsonAnnotations.JACKSON_JSON_IGNORE.getValue());
            if (Objects.nonNull(jacksonIgnore)) {
                // 该字段需要忽略
                return JsonAssistantPlugin.PLUGIN_ID_NAME;
            }
        }

        String annotationValue = "";
        if (recognitionFastJsonAnnotation) {
            // 检测是否含有 fastjson 注解
            PsiAnnotation fastJsonJsonField = psiField.getAnnotation(JsonAnnotations.FAST_JSON_JSON_FIELD.getValue());
            PsiAnnotation fastJson2JsonField = psiField.getAnnotation(JsonAnnotations.FAST_JSON2_JSON_FIELD.getValue());

            if (Objects.nonNull(fastJsonJsonField) || Objects.nonNull(fastJson2JsonField)) {
                // 是否忽略序列化
                String serialize = JavaUtil.getMemberValue(fastJsonJsonField, "serialize");
                String serialize2 = JavaUtil.getMemberValue(fastJson2JsonField, "serialize");
                if (Objects.equals(Boolean.FALSE.toString(), serialize) || Objects.equals(Boolean.FALSE.toString(), serialize2)) {
                    return JsonAssistantPlugin.PLUGIN_ID_NAME;
                }

                // 获取值
                annotationValue = JavaUtil.getMemberValue(fastJsonJsonField, "name");
                if (StrUtil.isBlank(annotationValue)) {
                    annotationValue = JavaUtil.getMemberValue(fastJson2JsonField, "name");
                }

                return annotationValue;
            }
        }

        if (recognitionJacksonAnnotation) {
            // 检测是否含有 Jackson 注解
            PsiAnnotation jacksonJsonProperty = psiField.getAnnotation(JsonAnnotations.JACKSON_JSON_PROPERTY.getValue());

            if (Objects.nonNull(jacksonJsonProperty)) {
                annotationValue = JavaUtil.getMemberValue(jacksonJsonProperty, "value");
            }
        }

        return annotationValue;
    }


    /**
     * 根据类全限定名获取其类型的默认值
     *
     * @param psiType 类型
     * @return 默认值
     */
    public static Object getDefaultValue(PsiField psiField, PsiType psiType, boolean includeRandomValues) {
        // 检查是否存在常量定义
        Object value = PsiUtil.computeConstantExpression(psiField);
        if (Objects.nonNull(value)) {
            return value;
        }

        String canonicalText = psiType.getCanonicalText();
        // 8大包装类皆不许继承
        // 防止类型为继承的类，光靠类型名称判断不够严谨
        if (PsiKeyword.BOOLEAN.equals(canonicalText) || Boolean.class.getName().equals(canonicalText)) {
            return RandomUtil.randomBoolean();
        } else if (PsiKeyword.CHAR.equals(canonicalText) || Character.class.getName().equals(canonicalText)) {
            return includeRandomValues ? RandomUtil.randomChar() : "";
        } else if (String.class.getName().equals(canonicalText)) {
            return includeRandomValues ? RandomUtil.randomStringUpper(5) : "";
        } else if (PsiKeyword.BYTE.equals(canonicalText) || Byte.class.getName().equals(canonicalText)
                || PsiKeyword.SHORT.equals(canonicalText) || Short.class.getName().equals(canonicalText)
                || PsiKeyword.INT.equals(canonicalText) || Integer.class.getName().equals(canonicalText)
                || PsiKeyword.LONG.equals(canonicalText) || Long.class.getName().equals(canonicalText)) {
            return includeRandomValues ? RandomUtil.randomInt(1000) : 0;
        } else if (PsiKeyword.FLOAT.equals(canonicalText) || Float.class.getName().equals(canonicalText)
                || PsiKeyword.DOUBLE.equals(canonicalText) || Double.class.getName().equals(canonicalText)
                || isAssignType(psiType, PluginConstant.BIGDECIMAL_FQN)) {
            return includeRandomValues ? RandomUtil.randomFloat() : new BigDecimal("0.1");
        } else if (isAssignType(psiType, PluginConstant.COLLECTION_FQN)) {
            return new ArrayList<>();
        } else if (isAssignType(psiType, PluginConstant.DATE_FQN)) {
            return DateUtil.format(new Date(), DatePattern.NORM_DATE_PATTERN);
        } else if (isAssignType(psiType, PluginConstant.TIME_FQN)) {
            return DateUtil.format(new Date(), DatePattern.NORM_TIME_PATTERN);
        } else if (isAssignType(psiType, PluginConstant.DATE_TIME_FQN)) {
            return DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);
        } else {
            return null;
        }
    }


    public static boolean isAssignType(PsiType psiType, String[] clsNameList) {
        // 顶级接口预判断（Collection 接口父接口只有 Iterable 接口）
        String canonicalText = psiType.getCanonicalText();
        for (String className : clsNameList) {
            if (canonicalText.startsWith(className)) {
                return true;
            }
        }

        Set<String> superTypeNameSet = new HashSet<>();
        collTypeName(psiType.getSuperTypes(), superTypeNameSet);

        if (CollUtil.isNotEmpty(superTypeNameSet)) {
            for (String typeName : superTypeNameSet) {
                if (Arrays.stream(clsNameList).anyMatch(typeName::startsWith)) {
                    return true;
                }
            }
        }

        return false;
    }


    private static void collTypeName(PsiType[] psiTypes, Set<String> superTypeNameSet) {
        for (PsiType psiType : psiTypes) {
            superTypeNameSet.add(psiType.getCanonicalText());
            PsiType[] superTypes = psiType.getSuperTypes();

            if (ArrayUtil.isNotEmpty(superTypes)) {
                collTypeName(superTypes, superTypeNameSet);
            }
        }
    }


    /**
     * 判断是否为引用类型（自己项目中的类）（不包括String、BigDecimal之类的）
     *
     * @param psiType 类型
     * @return true，引用类型；false，不为引用类型
     */
    public static boolean isApplicationClsType(PsiType psiType) {
        // 不为引用类型
        if ((!(psiType instanceof PsiClassReferenceType)) && (!(psiType instanceof PsiImmediateClassType))) {
            return false;
        }

        // 全限定名（基本类型就只有基本类型名 long、int）
        String canonicalText = psiType.getCanonicalText();

        // 排除可继承的类型，如果是 集合、BigDecimal、Date、Time等，不将其视为对象
        if (isAssignType(psiType, PluginConstant.COLLECTION_FQN)
                || isAssignType(psiType, PluginConstant.BIGDECIMAL_FQN)
                || isAssignType(psiType, PluginConstant.DATE_TIME_FQN)
                || isAssignType(psiType, PluginConstant.DATE_FQN)
                || isAssignType(psiType, PluginConstant.TIME_FQN)) {
            return false;
        }

        // 判断是否为java包其他类
        return !StrUtil.startWith(canonicalText, "java.");
    }

    /**
     * 用两种方法获取PsiClass
     *
     * @param dataContext 数据上下文
     * @return Class
     */
    public static PsiClass getPsiClass(DataContext dataContext) {
        PsiClass psiClass = null;

        try {
            // 一个类中可能存在几个内部类
            PsiClass[] psiClasses = getAllPsiClassByPsiFile(dataContext);

            if (ArrayUtil.isEmpty(psiClasses)) {
                return getCurrentPsiClassByOffset(dataContext);
            } else {
                // 单独Class
                if (psiClasses.length == 1) {
                    psiClass = psiClasses[0];
                } else {
                    // 偏移量获取
                    PsiClass curClz = getCurrentPsiClassByOffset(dataContext);
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

    public static PsiClass getPsiClass(PsiType psiType) {
        return psiType instanceof PsiClassReferenceType ? ((PsiClassReferenceType) psiType).resolve() : null;
    }

    public static PsiClass getPsiClass(PsiJavaCodeReferenceElement referenceElement) {
        PsiClass psiClass = null;
        PsiElement resolve = referenceElement.resolve();
        if (resolve instanceof PsiClass) {
            psiClass = (PsiClass) resolve;
        } else if (resolve instanceof PsiLocalVariable) {
            psiClass = getPsiClass(((PsiLocalVariable) resolve).getType());
        } else if (resolve instanceof PsiField) {
            PsiType psiType = ((PsiField) resolve).getType();
            if (isApplicationClsType(psiType)) {
                psiClass = PsiTypesUtil.getPsiClass(psiType);
            } else if (isCollectionOrArray(psiType)) {
                psiClass = getGenericTypeOfCollection(referenceElement.getProject(), psiType);
            }
        } else if (resolve instanceof PsiParameter) {
            psiClass = getPsiClass(((PsiParameter) resolve).getType());
        }

        return psiClass;
    }

    public static void addKeywordsToClass(PsiElementFactory factory, String keywordString, PsiClass psiClass) {
        PsiKeyword keyword = factory.createKeyword(keywordString);
        PsiModifierList modifierList = psiClass.getModifierList();
        if (Objects.nonNull(modifierList)) {
            modifierList.add(keyword);
        }
    }


    /**
     * 根据Java文件获取当前Class文件及所有内部类
     *
     * @param dataContext 数据上下文
     * @return Class
     */
    public static PsiClass[] getAllPsiClassByPsiFile(DataContext dataContext) {
        List<PsiClass> psiClassList = new ArrayList<>();
        PsiFile psiFile = PlatformUtil.getPsiFile(dataContext);

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
     * @param dataContext 数据上下文信息
     * @return class
     */
    public static PsiClass getCurrentPsiClassByOffset(DataContext dataContext) {
        PsiFile psiFile = PlatformUtil.getPsiFile(dataContext);
        Editor editor = PlatformUtil.getEditor(dataContext);

        if (Objects.nonNull(psiFile) && Objects.nonNull(editor) && (psiFile.getFileType() instanceof JavaFileType)) {
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
     * @param dataContext 数据上下文
     * @return true -> 处于；false -> 不处于
     */
    public static boolean isJavaFile(DataContext dataContext) {
        // 获取当前选中的 PsiClass
        PsiFile psiFile = dataContext.getData(CommonDataKeys.PSI_FILE);
        return psiFile instanceof PsiJavaFile;
    }

    /**
     * 当前 Java 类中是否存在属性
     *
     * @param dataContext 数据上下文
     * @return true -> 存在；false -> 不存在
     */
    public static boolean hasJavaProperty(DataContext dataContext) {
        PsiClass psiClass = getPsiClass(dataContext);
        return hasJavaProperty(psiClass);
    }

    /**
     * 当前 Java 类中是否存在属性
     *
     * @param psiClass 类
     * @return true -> 存在；false -> 不存在
     */
    public static boolean hasJavaProperty(PsiClass psiClass) {
        boolean enabled = false;
        if (Objects.nonNull(psiClass)) {
            PsiField[] fields = getNonStaticFields(psiClass);
            enabled = ArrayUtil.isNotEmpty(fields);
        }

        return enabled;
    }


    /**
     * 获取该类的所有字段（除去静态字段）
     *
     * @param psiClass class
     * @return 所有字段
     */
    public static PsiField[] getNonStaticFields(PsiClass psiClass) {
        return (Objects.isNull(psiClass))
                ? new PsiField[0]
                : Arrays.stream(psiClass.getAllFields()).filter(el -> !el.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiField[]::new);
    }

    /**
     * 获取该类的所有静态字段
     *
     * @param psiClass class
     * @return 所有静态字段
     */
    public static PsiField[] getStaticFields(PsiClass psiClass) {
        return (Objects.isNull(psiClass))
                ? new PsiField[0]
                : Arrays.stream(psiClass.getAllFields()).filter(el -> el.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiField[]::new);
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

        if (memberValue instanceof PsiLiteralExpression) {
            // value属性值
            value = memberValue.getText();
            if (StringUtils.isNotBlank(value)) {
                value = value.replace("\"", "");
            }
        } else if (memberValue instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) memberValue).resolve();

            if (resolve instanceof PsiField) {
                PsiExpression initializer = ((PsiField) resolve).getInitializer();
                if (Objects.nonNull(initializer)) {
                    String text = initializer.getText();
                    if (StringUtils.isNotBlank(text)) {
                        value = text.replace("\"", "");
                    }
                }
            }
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
            // 判断纯数字类型
            String numberType = NumberUtil.isNumber(str) ? Long.class.getSimpleName() : null;
            // 时间类型判断
            return Objects.isNull(numberType) ? isDateType(str) : numberType;

        } else if (obj instanceof BigDecimal) {
            type = Double.class.getSimpleName();

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
     * @param str 参数
     * @return 为时间类型返回 Date、否则返回 String
     */
    private static String isDateType(String str) {
        String type = String.class.getSimpleName();
        try {
            DateTime time = DateUtil.parse(str);
            if (Objects.nonNull(time)) {
                type = Date.class.getSimpleName();
            }
        } catch (DateException ignored) {
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
        if (StrUtil.isBlank(qualifiedName)) {
            return null;
        }

        PsiClass result = null;
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        if (qualifiedName.contains("$")) {
            String[] split = qualifiedName.split("\\$");
            String mainClassQName = split[0];
            String innerClassName = split[1];

            PsiClass mainClass = psiFacade.findClass(mainClassQName, GlobalSearchScope.allScope(project));
            if (null != mainClass) {
                result = mainClass.findInnerClassByName(innerClassName, true);
            }

        } else {
            result = psiFacade.findClass(qualifiedName, GlobalSearchScope.allScope(project));
        }

        return result;
    }

    /**
     * 查找指定模块中的类。
     *
     * @param module        模块对象
     * @param qualifiedName 类的全限定名
     * @return 匹配的PsiClass对象，如果没有找到则返回null
     */
    public static PsiClass findClass(Module module, String qualifiedName) {
        if (StrUtil.isBlank(qualifiedName)) {
            return null;
        }

        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(module.getProject());
        return psiFacade.findClass(qualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
    }

    public static boolean hasFastJsonLib(Module module) {
        return Objects.nonNull(JavaUtil.findClass(module, JsonAnnotations.FAST_JSON_JSON_FIELD.getValue()));
    }

    public static boolean hasFastJson2Lib(Module module) {
        return Objects.nonNull(JavaUtil.findClass(module, JsonAnnotations.FAST_JSON2_JSON_FIELD.getValue()));
    }

    public static boolean hasJacksonLib(Module module) {
        return Objects.nonNull(JavaUtil.findClass(module, JsonAnnotations.JACKSON_JSON_PROPERTY.getValue()));
    }

    public static boolean isMissingFastJsonLib(Module module) {
        return !hasFastJsonLib(module);
    }

    public static boolean isMissingFastJson2Lib(Module module) {
        return !hasFastJson2Lib(module);
    }

    public static boolean isMissingJacksonLib(Module module) {
        return !hasJacksonLib(module);
    }

}
