package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.service.persistent.SerializationSettings;
import cn.memoryzy.json.service.persistent.state.SerializationState;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类型转换工具：将 JavaBean / JSON 转换为 TypeScript 类型声明
 *
 * @author Memory
 * @since 2026/09/02
 */
public class TypeScriptUtil {

    /**
     * TypeScript 保留字，作为属性名时需要加引号
     */
    private static final Set<String> RESERVED_WORDS = Set.of(
            "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do",
            "else", "enum", "export", "extends", "false", "finally", "for", "function", "if", "import",
            "in", "instanceof", "new", "null", "return", "super", "switch", "this", "throw", "true",
            "try", "typeof", "var", "void", "while", "with", "as", "implements", "interface", "let",
            "package", "private", "protected", "public", "static", "yield", "any", "boolean", "number",
            "string", "symbol", "type", "unknown", "never", "object"
    );

    // ================================================================
    //                          JSON → TypeScript
    // ================================================================

    /**
     * 将 JSON/JSON5 包装对象转换为 TypeScript 类型声明文本
     *
     * @param wrapper        解析后的 JSON 包装对象
     * @param rootName       根类型名称（为空时使用 RootObject）
     * @param includeComment 是否将 JSON5 注释转换为 JSDoc
     * @return TypeScript 类型声明
     */
    public static String jsonToTypeScript(JsonWrapper wrapper, String rootName, boolean includeComment) {
        if (wrapper == null) {
            return "";
        }

        List<String> declarations = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        String rootTypeName = uniqueName(usedNames, sanitizeTypeName(StrUtil.blankToDefault(rootName, "RootObject")));

        if (wrapper.isObject()) {
            String body = buildObjectBody((ObjectWrapper) wrapper, rootTypeName, declarations, usedNames, includeComment);
            declarations.add(0, "export interface " + rootTypeName + " {\n" + body + "}");
        } else if (wrapper.isArray()) {
            String itemType = buildArrayType((ArrayWrapper) wrapper, rootTypeName + "Item", declarations, usedNames, includeComment);
            declarations.add(0, "export type " + rootTypeName + " = " + itemType + ";");
        }

        return StrUtil.join("\n\n", declarations);
    }

    private static String buildObjectBody(ObjectWrapper jsonObject, String typeName, List<String> declarations,
                                          Set<String> usedNames, boolean includeComment) {
        Map<?, ?> commentsMap = includeComment ? Json5Util.getCommentsMap(jsonObject) : null;
        StringBuilder body = new StringBuilder();

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            // 跳过注释键
            if (PluginConstant.COMMENT_KEY.equals(key)) {
                continue;
            }

            Object value = entry.getValue();
            String propertyName = sanitizePropertyName(key);
            String comment = includeComment ? Json5Util.getComment(commentsMap, key) : null;

            String type;
            if (value instanceof ObjectWrapper) {
                // 嵌套对象 -> 生成子接口
                String childName = uniqueName(usedNames, StrUtil.upperFirst(sanitizeTypeName(key)));
                String childBody = buildObjectBody((ObjectWrapper) value, childName, declarations, usedNames, includeComment);
                declarations.add("export interface " + childName + " {\n" + childBody + "}");
                type = childName;
            } else if (value instanceof ArrayWrapper) {
                // 数组
                type = buildArrayType((ArrayWrapper) value, StrUtil.upperFirst(sanitizeTypeName(key)), declarations, usedNames, includeComment);
            } else {
                type = jsonValueToTsType(value);
            }

            if (StrUtil.isNotBlank(comment)) {
                body.append("  /** ").append(sanitizeComment(comment)).append(" */\n");
            }
            body.append("  ").append(propertyName).append(": ").append(type).append(";\n");
        }

        return body.toString();
    }

    private static String buildArrayType(ArrayWrapper array, String itemBaseName, List<String> declarations,
                                         Set<String> usedNames, boolean includeComment) {
        if (array.isEmpty()) {
            return "any[]";
        }

        Object element = array.get(0);
        if (element instanceof ObjectWrapper) {
            // 数组元素为对象 -> 生成子接口
            String itemName = uniqueName(usedNames, itemBaseName);
            String itemBody = buildObjectBody((ObjectWrapper) element, itemName, declarations, usedNames, includeComment);
            declarations.add("export interface " + itemName + " {\n" + itemBody + "}");
            return itemName + "[]";
        }

        return jsonValueToTsType(element) + "[]";
    }

    private static String jsonValueToTsType(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "string";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Number) {
            return "number";
        }
        return "any";
    }

    // ================================================================
    //                        JavaBean → TypeScript
    // ================================================================

    /**
     * 将 JavaBean 转换为 TypeScript 类型声明文本
     *
     * @param project        项目对象
     * @param psiClass       Java 类
     * @param resolveComment 是否解析字段注释为 JSDoc
     * @return TypeScript 类型声明
     */
    public static String javaBeanToTypeScript(Project project, PsiClass psiClass, boolean resolveComment) {
        if (project == null || psiClass == null) {
            return "";
        }

        List<String> declarations = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        Set<String> processedQualifiedNames = new HashSet<>();
        Map<String, String> qualifiedNameToTypeName = new HashMap<>();
        SerializationState serializationState = SerializationSettings.getInstance().getSerializationState();

        String rootName = uniqueName(usedNames, sanitizeTypeName(StrUtil.blankToDefault(psiClass.getName(), "RootObject")));
        // 注册根类，避免自引用时无限递归
        String rootQualifiedName = psiClass.getQualifiedName();
        if (StrUtil.isNotBlank(rootQualifiedName)) {
            qualifiedNameToTypeName.put(rootQualifiedName, rootName);
            processedQualifiedNames.add(rootQualifiedName);
        }

        String rootBody = buildJavaObjectBody(project, psiClass, serializationState, rootName, declarations, usedNames,
                processedQualifiedNames, qualifiedNameToTypeName, resolveComment);
        declarations.add(0, "export interface " + rootName + " {\n" + rootBody + "}");

        return StrUtil.join("\n\n", declarations);
    }

    private static String buildJavaObjectBody(Project project, PsiClass psiClass, SerializationState serializationState,
                                              String typeName, List<String> declarations, Set<String> usedNames,
                                              Set<String> processedQualifiedNames, Map<String, String> qualifiedNameToTypeName,
                                              boolean resolveComment) {
        StringBuilder body = new StringBuilder();
        PsiField[] fields = JavaUtil.getNonStaticFields(psiClass);

        for (PsiField field : fields) {
            String fieldName = field.getName();
            // 忽略 transient 字段
            if (field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }

            // 支持 @JsonProperty / @JsonField 等注解重命名与忽略
            String jsonKeyName = JavaUtil.getAnnotationJsonKeyName(field, serializationState);
            // 返回 PLUGIN_ID_NAME 表示忽略该字段
            if (JsonAssistantPlugin.PLUGIN_ID_NAME.equals(jsonKeyName)) {
                continue;
            }
            String propertyName = StrUtil.isBlank(jsonKeyName) ? fieldName : jsonKeyName;

            String comment = resolveComment ? JavaUtil.resolveFieldComment(field) : null;
            String tsType = mapJavaTypeToTs(project, field, field.getType(), typeName, declarations, usedNames,
                    processedQualifiedNames, qualifiedNameToTypeName, serializationState, resolveComment);

            if (StrUtil.isNotBlank(comment)) {
                body.append("  /** ").append(sanitizeComment(comment)).append(" */\n");
            }
            body.append("  ").append(sanitizePropertyName(propertyName)).append(": ").append(tsType).append(";\n");
        }

        return body.toString();
    }


    private static String mapJavaTypeToTs(Project project, PsiField field, PsiType psiType, String ownerTypeName,
                                          List<String> declarations, Set<String> usedNames, Set<String> processedQualifiedNames,
                                          Map<String, String> qualifiedNameToTypeName, SerializationState serializationState,
                                          boolean resolveComment) {
        // 数组（含基本类型数组）
        if (psiType instanceof com.intellij.psi.PsiArrayType) {
            PsiType componentType = ((com.intellij.psi.PsiArrayType) psiType).getComponentType();
            String componentTs = mapJavaTypeToTs(project, field, componentType, ownerTypeName, declarations, usedNames,
                    processedQualifiedNames, qualifiedNameToTypeName, serializationState, resolveComment);
            return componentTs + "[]";
        }

        // 集合（List/Set 等）或数组
        if (JavaUtil.isCollectionOrArray(psiType)) {
            PsiClass elementClass = JavaUtil.getGenericTypeOfCollection(project, psiType);
            if (elementClass == null) {
                return "any[]";
            }
            String elementTs = mapPsiClassToTs(project, elementClass, declarations, usedNames, processedQualifiedNames,
                    qualifiedNameToTypeName, serializationState, resolveComment);
            return elementTs + "[]";
        }

        // Map<K, V> -> Record<string, V>
        if (isMapType(psiType)) {
            PsiType valueType = getMapValueType(psiType);
            String valueTs = (valueType == null) ? "any"
                    : mapJavaTypeToTs(project, field, valueType, ownerTypeName, declarations, usedNames,
                    processedQualifiedNames, qualifiedNameToTypeName, serializationState, resolveComment);
            return "Record<string, " + valueTs + ">";
        }

        PsiClass fieldClass = PsiTypesUtil.getPsiClass(psiType);
        return mapPsiClassToTs(project, fieldClass, declarations, usedNames, processedQualifiedNames,
                qualifiedNameToTypeName, serializationState, resolveComment);
    }

    private static boolean isMapType(PsiType psiType) {
        return JavaUtil.isTypeAssignableToAny(psiType, PluginConstant.MAP_FQN);
    }

    private static PsiType getMapValueType(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            PsiType[] parameters = ((PsiClassType) psiType).getParameters();
            if (parameters.length == 2) {
                return parameters[1];
            }
        }
        return null;
    }

    private static String mapPsiClassToTs(Project project, PsiClass fieldClass, List<String> declarations,
                                          Set<String> usedNames, Set<String> processedQualifiedNames,
                                          Map<String, String> qualifiedNameToTypeName, SerializationState serializationState,
                                          boolean resolveComment) {
        if (fieldClass == null) {
            return "any";
        }

        String canonical = fieldClass.getQualifiedName();
        String name = fieldClass.getName();

        // 基本类型（无全限定名）
        if (canonical == null) {
            if (name != null) {
                switch (name) {
                    case "boolean":
                        return "boolean";
                    case "char":
                    case "byte":
                    case "short":
                    case "int":
                    case "long":
                    case "float":
                    case "double":
                        return "number";
                    default:
                        break;
                }
            }
            return "any";
        }

        // JDK 常用类型映射
        switch (canonical) {
            case "java.lang.String":
            case "java.lang.CharSequence":
            case "java.lang.Character":
            case "java.lang.StringBuilder":
            case "java.lang.StringBuffer":
            case "java.util.UUID":
                return "string";
            case "java.lang.Boolean":
                return "boolean";
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.math.BigDecimal":
            case "java.math.BigInteger":
                return "number";
            case "java.util.Date":
            case "java.sql.Date":
            case "java.sql.Time":
            case "java.sql.Timestamp":
            case "java.time.LocalDate":
            case "java.time.LocalTime":
            case "java.time.LocalDateTime":
            case "java.time.Instant":
            case "java.time.OffsetDateTime":
            case "java.time.ZonedDateTime":
                return "string";
            case "java.lang.Object":
                return "any";
            default:
                break;
        }

        // 枚举 -> 联合类型
        if (fieldClass.isEnum()) {
            List<String> constants = new ArrayList<>();
            PsiField[] staticFields = JavaUtil.getStaticFields(fieldClass);
            for (PsiField staticField : staticFields) {
                String constantName = staticField.getName();
                if (StrUtil.isNotBlank(constantName) && !constantName.startsWith("$")) {
                    constants.add("'" + constantName + "'");
                }
            }
            return constants.isEmpty() ? "string" : StrUtil.join(" | ", constants);
        }

        // 其他 JDK 类型（无法确定序列化形态），不深入解析
        if (canonical.startsWith("java.") || canonical.startsWith("javax.") || canonical.startsWith("jdk.")
                || canonical.startsWith("kotlin.") || canonical.startsWith("kotlinx.")) {
            return "any";
        }

        // 自定义类 -> 递归生成接口
        String existingName = qualifiedNameToTypeName.get(canonical);
        if (existingName != null) {
            return existingName;
        }

        String typeName = uniqueName(usedNames, sanitizeTypeName(StrUtil.blankToDefault(fieldClass.getName(), "AnyObject")));
        qualifiedNameToTypeName.put(canonical, typeName);

        if (!processedQualifiedNames.contains(canonical)) {
            processedQualifiedNames.add(canonical);
            String childBody = buildJavaObjectBody(project, fieldClass, serializationState, typeName, declarations,
                    usedNames, processedQualifiedNames, qualifiedNameToTypeName, resolveComment);
            declarations.add("export interface " + typeName + " {\n" + childBody + "}");
        }

        return typeName;
    }

    // ================================================================
    //                            通用工具
    // ================================================================

    /**
     * 生成不与已有名称冲突的类型名
     */
    private static String uniqueName(Set<String> usedNames, String baseName) {
        String name = baseName;
        int index = 2;
        while (usedNames.contains(name)) {
            name = baseName + index++;
        }
        usedNames.add(name);
        return name;
    }

    /**
     * 清洗类型名：去除非法字符，保证以字母/下划线/美元符开头
     */
    private static String sanitizeTypeName(String name) {
        String cleaned = StrUtil.blankToDefault(name, "RootObject").replaceAll("[^A-Za-z0-9_$]", "");
        if (cleaned.isEmpty()) {
            cleaned = "RootObject";
        }
        char first = cleaned.charAt(0);
        if (!Character.isLetter(first) && first != '_' && first != '$') {
            cleaned = "_" + cleaned;
        }
        return cleaned;
    }

    /**
     * 清洗属性名：合法标识符原样输出，否则用双引号包裹
     */
    private static String sanitizePropertyName(String key) {
        if (StrUtil.isBlank(key)) {
            return "\"\"";
        }
        if (key.matches("[A-Za-z_$][A-Za-z0-9_$]*") && !RESERVED_WORDS.contains(key)) {
            return key;
        }
        return "\"" + key.replace("\"", "\\\"") + "\"";
    }

    /**
     * 清洗注释：合并空白、转义注释结束符
     */
    private static String sanitizeComment(String comment) {
        if (comment == null) {
            return "";
        }
        return comment.replaceAll("\\s+", " ").trim().replace("*/", "*\\/");
    }
}
