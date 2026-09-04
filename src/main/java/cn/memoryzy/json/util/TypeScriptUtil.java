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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ================================================================
    //                     TypeScript → JSON（剪贴板策略）
    // ================================================================

    /**
     * TypeScript 类型声明匹配正则
     */
    private static final Pattern DECLARATION_PATTERN = Pattern.compile(
            "\\b(interface|type)\\s+([A-Za-z_$][A-Za-z0-9_$]*)(?:\\s*<[^>]*)?");

    /**
     * 判断给定文本是否为可转换为 JSON 的 TypeScript 类型声明
     *
     * @param text 剪贴板文本
     * @return 可转换返回 true
     */
    public static boolean canConvert(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        try {
            return !parseDeclarations(text).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将 TypeScript 类型声明转换为 JSON 示例文本
     *
     * @param text 剪贴板文本（TypeScript 类型声明）
     * @return JSON 文本，无法转换时返回空字符串
     */
    public static String convertToJson(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }

        try {
            Map<String, List<TsProperty>> declarations = parseDeclarations(text);
            if (declarations.isEmpty()) {
                return "";
            }

            String rootName = declarations.keySet().iterator().next();
            Set<String> resolving = new HashSet<>();
            resolving.add(rootName);
            Map<String, Object> root = buildTsObject(declarations.get(rootName), declarations, resolving);

            return JsonUtil.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * TypeScript 类型声明中的属性
     */
    private static class TsProperty {

        final String name;

        final String type;

        final boolean optional;

        TsProperty(String name, String type, boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
        }
    }

    /**
     * 解析文本中的所有 TypeScript interface/type 声明
     */
    private static Map<String, List<TsProperty>> parseDeclarations(String text) {
        Map<String, List<TsProperty>> declarations = new LinkedHashMap<>();
        String cleaned = stripComments(text);
        Matcher matcher = DECLARATION_PATTERN.matcher(cleaned);
        while (matcher.find()) {
            String kind = matcher.group(1);
            String name = matcher.group(2);
            int openBrace = cleaned.indexOf('{', matcher.end());
            if (openBrace < 0) {
                continue;
            }

            String between = cleaned.substring(matcher.end(), openBrace);
            // type 别名必须是对象字面量（含 '='）
            if ("type".equals(kind) && !between.contains("=")) {
                continue;
            }

            int closeBrace = findMatchingBrace(cleaned, openBrace);
            if (closeBrace < 0) {
                continue;
            }

            String body = cleaned.substring(openBrace + 1, closeBrace);
            declarations.put(name, parseTsTypeBody(body));
        }

        return declarations;
    }

    /**
     * 解析 TypeScript 类型体，提取属性列表
     */
    private static List<TsProperty> parseTsTypeBody(String body) {
        List<TsProperty> props = new ArrayList<>();
        for (String raw : splitTopLevel(body)) {
            String member = StrUtil.trim(raw);
            if (member.isEmpty()) {
                continue;
            }

            // 跳过索引签名，如 [key: string]: X
            if (member.startsWith("[")) {
                continue;
            }

            int colon = indexOfTopLevelColon(member);
            if (colon < 0) {
                continue;
            }

            String keyPart = StrUtil.trim(member.substring(0, colon));
            String typePart = StrUtil.trim(member.substring(colon + 1));
            // 跳过方法声明，如 foo(): T
            if (keyPart.contains("(") || keyPart.isEmpty()) {
                continue;
            }

            boolean optional = false;
            if (keyPart.endsWith("?")) {
                optional = true;
                keyPart = StrUtil.trim(keyPart.substring(0, keyPart.length() - 1));
            }

            // 去除引号包裹的键名
            if ((keyPart.startsWith("\"") && keyPart.endsWith("\""))
                    || (keyPart.startsWith("'") && keyPart.endsWith("'"))) {
                keyPart = keyPart.substring(1, keyPart.length() - 1);
            }

            if (keyPart.isEmpty()) {
                continue;
            }

            props.add(new TsProperty(keyPart, typePart, optional));
        }

        return props;
    }

    /**
     * 构建 TypeScript 类型对应的示例 JSON 对象
     */
    private static Map<String, Object> buildTsObject(List<TsProperty> props, Map<String, List<TsProperty>> declarations,
                                                    Set<String> resolving) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (props == null) {
            return map;
        }

        for (TsProperty prop : props) {
            map.put(prop.name, typeToJson(prop.type, declarations, resolving));
        }

        return map;
    }

    /**
     * 将 TypeScript 类型表达式转换为示例 JSON 值
     */
    private static Object typeToJson(String typeExpr, Map<String, List<TsProperty>> declarations, Set<String> resolving) {
        String t = StrUtil.trim(typeExpr);
        if (t.isEmpty()) {
            return null;
        }

        // 联合类型 -> 取第一个
        List<String> unions = splitTopLevelChar(t, '|');
        if (unions.size() > 1) {
            return typeToJson(StrUtil.trim(unions.get(0)), declarations, resolving);
        }

        // 交叉类型 -> 合并对象
        List<String> intersects = splitTopLevelChar(t, '&');
        if (intersects.size() > 1) {
            Map<String, Object> merged = new LinkedHashMap<>();
            for (String part : intersects) {
                Object value = typeToJson(StrUtil.trim(part), declarations, resolving);
                if (value instanceof Map) {
                    merged.putAll((Map<String, Object>) value);
                }
            }
            return merged;
        }

        // 数组
        if (t.endsWith("[]") || (t.startsWith("Array<") && t.endsWith(">"))
                || (t.startsWith("ReadonlyArray<") && t.endsWith(">"))) {
            return new ArrayList<>();
        }

        // Record / Map
        if ((t.startsWith("Record<") || t.startsWith("Map<")) && t.endsWith(">")) {
            return new LinkedHashMap<>();
        }

        // 内联对象类型 { ... }
        if (t.startsWith("{")) {
            int close = findMatchingBrace(t, 0);
            if (close > 0) {
                return buildTsObject(parseTsTypeBody(t.substring(1, close)), declarations, resolving);
            }
            return new LinkedHashMap<>();
        }

        // 字符串字面量
        if ((t.startsWith("'") && t.endsWith("'")) || (t.startsWith("\"") && t.endsWith("\""))) {
            return t.substring(1, t.length() - 1);
        }

        // 数字字面量
        if (t.matches("-?\\d+(\\.\\d+)?")) {
            try {
                return Long.parseLong(t);
            } catch (NumberFormatException e) {
                try {
                    return Double.parseDouble(t);
                } catch (NumberFormatException ignored) {
                    return t;
                }
            }
        }

        // 布尔 / null 字面量
        switch (t) {
            case "true":
                return Boolean.TRUE;
            case "false":
                return Boolean.FALSE;
            case "null":
                return null;
            default:
                break;
        }

        // 基本类型
        switch (t) {
            case "string":
                return "";
            case "number":
                return 0;
            case "boolean":
                return Boolean.FALSE;
            case "Date":
                return "";
            default:
                break;
        }

        // 任意/未知等 -> null
        if ("any".equals(t) || "unknown".equals(t) || "void".equals(t) || "undefined".equals(t) || "never".equals(t)) {
            return null;
        }

        // 函数类型 -> null
        if (t.contains("=>") || t.startsWith("(")) {
            return null;
        }

        // 泛型引用，如 Page<User>
        String baseName = t;
        int genericStart = t.indexOf('<');
        if (genericStart > 0 && t.endsWith(">")) {
            baseName = StrUtil.trim(t.substring(0, genericStart));
        }

        // 引用其他接口
        if (declarations.containsKey(baseName)) {
            if (resolving.contains(baseName)) {
                return new LinkedHashMap<>();
            }
            resolving.add(baseName);
            Object obj = buildTsObject(declarations.get(baseName), declarations, resolving);
            resolving.remove(baseName);
            return obj;
        }

        return null;
    }

    /**
     * 按顶层分隔符拆分（不拆分字符串与括号内的内容）
     */
    private static List<String> splitTopLevelChar(String s, char sep) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"' || c == '\'' || c == '`') {
                int end = skipString(s, i);
                cur.append(s, i, end);
                i = end;
                continue;
            }
            if (c == '{' || c == '[' || c == '(' || c == '<') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')' || c == '>') {
                depth--;
            }
            if (depth == 0 && c == sep) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
            i++;
        }
        if (cur.length() > 0) {
            parts.add(cur.toString());
        }
        return parts;
    }

    /**
     * 按顶层分号或逗号拆分类型体
     */
    private static List<String> splitTopLevel(String body) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        int i = 0;
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c == '"' || c == '\'' || c == '`') {
                int end = skipString(body, i);
                cur.append(body, i, end);
                i = end;
                continue;
            }
            if (c == '{' || c == '[' || c == '(' || c == '<') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')' || c == '>') {
                depth--;
            }
            if (depth == 0 && (c == ';' || c == ',')) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
            i++;
        }
        if (cur.length() > 0) {
            parts.add(cur.toString());
        }
        return parts;
    }

    /**
     * 查找顶层冒号位置
     */
    private static int indexOfTopLevelColon(String s) {
        int depth = 0;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"' || c == '\'' || c == '`') {
                i = skipString(s, i);
                continue;
            }
            if (c == '{' || c == '[' || c == '(' || c == '<') {
                depth++;
            } else if (c == '}' || c == ']' || c == ')' || c == '>') {
                depth--;
            } else if (c == ':' && depth == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 跳过字符串字面量，返回字符串结束后的下标
     */
    private static int skipString(String s, int start) {
        char quote = s.charAt(start);
        int i = start + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return s.length();
    }

    /**
     * 查找与左花括号匹配的右花括号下标
     */
    private static int findMatchingBrace(String s, int openIndex) {
        int depth = 0;
        int i = openIndex;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"' || c == '\'' || c == '`') {
                i = skipString(s, i);
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    /**
     * 去除注释（保留字符串字面量内容）
     */
    private static String stripComments(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'' || c == '`') {
                int end = skipString(text, i);
                sb.append(text, i, end);
                i = end;
                continue;
            }
            if (c == '/' && i + 1 < n && text.charAt(i + 1) == '/') {
                int end = text.indexOf('\n', i);
                if (end < 0) {
                    end = n;
                }
                i = end;
                continue;
            }
            if (c == '/' && i + 1 < n && text.charAt(i + 1) == '*') {
                int end = text.indexOf("*/", i + 2);
                if (end < 0) {
                    end = n - 2;
                }
                i = end + 2;
                continue;
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
}
