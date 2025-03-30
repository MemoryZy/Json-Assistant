package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.debug.RuntimeObjectToJsonAction;
import cn.memoryzy.json.constant.LanguageHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.FileTypes;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.events.DebuggerContextCommandImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XEvaluationCallbackBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class JavaDebugUtil {

    /**
     * 判断DataContext中选中的节点是否为对象或者含有子元素的列表。
     *
     * @param project     项目
     * @param dataContext 当前数据上下文环境，包含了当前操作需要的所有信息。
     * @return 如果选中的节点是对象或含有子元素的列表，则返回true；否则返回false。
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isObjectOrListWithChildren(@Nullable Project project, DataContext dataContext) {
        XDebuggerTree tree = XDebuggerTree.getTree(dataContext);
        if (Objects.isNull(tree)) {
            return false;
        }

        TreePath[] selectionPaths = tree.getSelectionPaths();
        // 只允许选择一个节点
        if (ArrayUtil.isEmpty(selectionPaths) || selectionPaths.length > 1) {
            return false;
        }

        TreePath path = selectionPaths[0];
        XValueNodeImpl selectedNode = getSelectedNode(path);
        XValue selectedValue = getSelectedValue(selectedNode);

        // 判断是否为 Java-Debug 模式
        if (!(selectedValue instanceof JavaValue)) {
            return false;
        }

        // 获取类全限定名，判断是否为JavaBean，若是，则取该节点下的那些节点（递归）组合成JSON，再创建标签页
        ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
        Value value = descriptor.getValue();

        if (Objects.isNull(value) || Objects.isNull(value.type())) {
            return false;
        }

        // 非对象或字符
        if (!(value instanceof ObjectReference) || value instanceof StringReference) {
            return false;
        }

        ObjectReference objectValue = (ObjectReference) value;
        if (objectValue instanceof ArrayReference) {
            return ((ArrayReference) objectValue).length() > 0;

        } else if (isCollection(objectValue) || isMap(objectValue)) {
            return hasElements(project, objectValue);

        } else {
            // 拒绝包装类型及JDK内部类型
            if (!isBeanType(project, objectValue)) {
                return false;
            }

            return hasFields(objectValue);
        }
    }

    /**
     * 判断选中的节点是否为 JavaBean 或存在 JavaBean 的集合
     *
     * @return 为 JavaBean 或存在 JavaBean 的集合，则为true；反之为false
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isJavaBeanOrContainsJavaBeans(Project project, DataContext dataContext) {
        XDebuggerTree tree = XDebuggerTree.getTree(dataContext);
        if (Objects.isNull(tree)) {
            return false;
        }

        TreePath[] selectionPaths = tree.getSelectionPaths();
        // 只允许选择一个节点
        if (ArrayUtil.isEmpty(selectionPaths) || selectionPaths.length > 1) {
            return false;
        }

        TreePath path = selectionPaths[0];
        XValueNodeImpl selectedNode = getSelectedNode(path);
        XValue selectedValue = getSelectedValue(selectedNode);

        // 判断是否为 Java-Debug 模式
        if (!(selectedValue instanceof JavaValue)) {
            return false;
        }

        // 获取类全限定名，判断是否为JavaBean，若是，则取该节点下的那些节点（递归）组合成JSON，再创建标签页
        ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
        Value value = descriptor.getValue();

        if (Objects.isNull(value) || Objects.isNull(value.type())) {
            return false;
        }

        // 非对象或字符
        if (!(value instanceof ObjectReference) || value instanceof StringReference) {
            return false;
        }

        ObjectReference objectValue = (ObjectReference) value;
        if (objectValue instanceof ArrayReference) {
            // 数组判断
            return areArrayElementsJavaBeans(project, (ArrayReference) objectValue);

        } else if (isCollection(objectValue)) {
            // 集合判断，不处理 Map 类型
            return areCollectionElementsJavaBeans(project, objectValue);

        } else {
            // 拒绝包装类型及JDK内部类型
            if (!isBeanType(project, objectValue)) {
                return false;
            }

            return hasFields(objectValue);
        }
    }

    private static boolean areCollectionElementsJavaBeans(Project project, ObjectReference reference) {
        // toArray 之后就是数组
        Value value = invokeMethod(project, reference, "toArray");
        if (value != null) {
            return areArrayElementsJavaBeans(project, (ArrayReference) value);
        }

        return false;
    }

    private static boolean areArrayElementsJavaBeans(Project project, ArrayReference arrayReference) {
        if (arrayReference.length() <= 0) {
            return false;
        }

        // 若存在JavaBean类型，返回true；反之为false
        return arrayReference.getValues().stream().anyMatch(value -> value instanceof ObjectReference && isBeanType(project, (ObjectReference) value));
    }

    /**
     * 处理复杂节点（对象、集合、Map、数组）
     *
     * @param project     项目
     * @param dataContext 数据上下文
     * @return 根据节点类型返回对应类型值
     */
    public static Object resolveObjectReference(Project project, DataContext dataContext) {
        // 获取调试树
        XDebuggerTree tree = XDebuggerTree.getTree(dataContext);
        if (Objects.isNull(tree)) {
            return null;
        }

        // 选中节点
        TreePath[] selectionPaths = tree.getSelectionPaths();
        // 只允许选择一个节点
        if (ArrayUtil.isEmpty(selectionPaths) || selectionPaths.length > 1) {
            return null;
        }

        TreePath path = selectionPaths[0];
        XValueNodeImpl selectedNode = getSelectedNode(path);
        XValue selectedValue = getSelectedValue(selectedNode);

        // 判断是否为 Java-Debug 模式
        if (!(selectedValue instanceof JavaValue)) {
            return null;
        }

        // 获取类全限定名，判断是否为JavaBean，若是，则取该节点下的那些节点（递归）组合成JSON，再创建标签页
        ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
        Value value = descriptor.getValue();

        // 值为空
        if (Objects.isNull(value) || Objects.isNull(value.type())) {
            return null;
        }

        // 对象类型（包括集合）
        return (value instanceof ObjectReference) && !(value instanceof StringReference) ? getValue(project, value) : null;
    }


    /**
     * 根据提供的项目、父节点和值对象，检索并返回相应类型的值。
     *
     * @param project 当前项目上下文，用于获取必要的环境信息。
     * @param value   要检索其具体值的对象。
     * @return 返回值的具体表示形式，可能是原始类型、字符串、数组、集合、映射或普通对象。
     */
    public static Object getValue(Project project, Value value) {
        if (value == null) {
            return null;
        }

        if (value instanceof PrimitiveValue) {
            return getPrimitiveValue((PrimitiveValue) value);

        } else if (value instanceof StringReference) {
            return getStringValue((StringReference) value);

        } else if (value instanceof ArrayReference) {
            return getArrayValue(project, (ArrayReference) value);

        } else {
            ObjectReference reference = (ObjectReference) value;
            if (isCollection(reference)) {
                return getCollectionValue(project, reference);

            } else if (isMap(reference)) {
                return getMapValue(project, reference);

            } else {
                return getObjectValue(project, reference);
            }
        }
    }


    /**
     * 获取Map对象值
     *
     * @param reference 要检索其具体值的对象
     * @return Map对象值
     */
    private static Object getMapValue(Project project, ObjectReference reference) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<ImmutablePair<Value, Value>> immutablePairs = invokeMapMethod(project, reference);
        for (ImmutablePair<Value, Value> pair : immutablePairs) {
            Object key = getValue(project, pair.getKey());
            Object value = getValue(project, pair.getValue());
            resultMap.put(String.valueOf(key), value);
        }

        return resultMap;
    }

    /**
     * 获取集合值
     *
     * @param reference 要检索其具体值的对象
     * @return 集合值
     */
    private static Object getCollectionValue(Project project, ObjectReference reference) {
        Value value = invokeMethod(project, reference, "toArray");
        if (value != null) {
            return getValue(project, value);
        }

        return List.of();
    }

    /**
     * 获取数组值
     *
     * @param project        项目
     * @param arrayReference 要检索其具体值的对象
     * @return 数组值
     */
    public static List<Object> getArrayValue(Project project, ArrayReference arrayReference) {
        List<Object> resultList = new ArrayList<>();
        List<Value> childrenValues = arrayReference.getValues();
        for (Value childrenValue : childrenValues) {
            resultList.add(getValue(project, childrenValue));
        }

        return resultList;
    }


    /**
     * 获取字符串值
     *
     * @param stringReference 要检索其具体值的对象
     * @return 字符串值
     */
    public static String getStringValue(StringReference stringReference) {
        return stringReference.value();
    }

    /**
     * 获取基本类型值
     *
     * @param primitiveValue 要检索其具体值的对象
     * @return 类型值
     */
    public static Object getPrimitiveValue(PrimitiveValue primitiveValue) {
        // 根据字段类型返回对应值
        switch (primitiveValue.type().name()) {
            case PsiKeyword.LONG:
                return primitiveValue.longValue();
            case PsiKeyword.INT:
                return primitiveValue.intValue();
            case PsiKeyword.DOUBLE:
                return primitiveValue.doubleValue();
            case PsiKeyword.FLOAT:
                return primitiveValue.floatValue();
            case PsiKeyword.BYTE:
                return primitiveValue.byteValue();
            case PsiKeyword.SHORT:
                return primitiveValue.shortValue();
            case PsiKeyword.CHAR:
                return primitiveValue.charValue();
            case PsiKeyword.BOOLEAN:
                return primitiveValue.booleanValue();
            default:
                return null;
        }
    }

    /**
     * 获取对象类型值
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 对象类型值
     */
    public static Object getObjectValue(Project project, ObjectReference objectReference) {
        String typeName = objectReference.referenceType().name();
        // 处理常用包装类型
        switch (typeName) {
            case "java.lang.Long":
            case "java.lang.Integer":
            case "java.lang.Double":
            case "java.lang.Float":
            case "java.lang.Short":
            case "java.lang.Byte":
            case "java.lang.Character":
            case "java.lang.Boolean":
                return getFieldValue(project, objectReference, "value");
            case "java.lang.String":
                return getStringValue((StringReference) objectReference);
            case "java.util.Date":
                return getDateValue(project, objectReference);
            case "java.sql.Time":
                return getTimeValue(project, objectReference);
            case "java.time.LocalDateTime":
                return getLocalDateTimeValue(project, objectReference);
            case "java.time.LocalDate":
                return getLocalDateValue(project, objectReference);
            case "java.time.LocalTime":
                return getLocalTimeValue(project, objectReference);
            case "java.math.BigDecimal":
                return getBigDecimalValue(project, objectReference);
            default:
                return getBeanValue(project, objectReference);
        }
    }


    /**
     * 获取Bean类型所有属性值列表
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 类型属性值列表
     */
    private static Object getBeanValue(Project project, ObjectReference objectReference) {
        if (!isBeanType(project, objectReference)) {
            return null;
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<Field> fields = getFields(objectReference);
        for (Field field : fields) {
            Value value = objectReference.getValue(field);
            resultMap.put(field.name(), getValue(project, value));
        }

        Boolean resolveComment = project.getUserData(RuntimeObjectToJsonAction.RESOLVE_COMMENT_KEY);
        if (BooleanUtil.isTrue(resolveComment)) {
            String name = objectReference.referenceType().name();
            PsiClass psiClass = JavaUtil.findClass(project, name);
            if (psiClass != null) {
                Map<String, String> commentMap = new HashMap<>();
                PsiField[] psiFields = psiClass.getAllFields();

                for (PsiField psiField : psiFields) {
                    String fieldName = psiField.getName();
                    PsiDocComment docComment = psiField.getDocComment();
                    if (docComment != null) {
                        String comment = JavaUtil.getDocComment(docComment);
                        if (StrUtil.isNotBlank(comment)) {
                            commentMap.put(fieldName, comment);
                        }
                    }
                }

                resultMap.put(PluginConstant.COMMENT_KEY, commentMap);
            }
        }

        return resultMap;
    }


    /**
     * 获取BigDecimal值
     *
     * @param objectReference 要检索其具体值的对象
     * @return BigDecimal值
     */
    private static BigDecimal getBigDecimalValue(Project project, ObjectReference objectReference) {
        Object stringCache = getFieldValue(project, objectReference, "stringCache");
        if (stringCache instanceof String) {
            return new BigDecimal((String) stringCache);
        }

        return null;
    }


    /**
     * 获取时间值 {@link java.util.Date}
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 时间值
     */
    public static String getDateValue(Project project, ObjectReference objectReference) {
        Object fastTime = getFieldValue(project, objectReference, "fastTime");
        if (fastTime instanceof Long) {
            long timestamp = (Long) fastTime;
            return JsonAssistantUtil.formatDateBasedOnTimestampDetails(timestamp);
        }

        return null;
    }

    /**
     * 获取时间值 {@link java.sql.Time}
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 时间值
     */
    private static Object getTimeValue(Project project, ObjectReference objectReference) {
        Object fastTime = getFieldValue(project, objectReference, "fastTime");
        if (fastTime instanceof Long) {
            LocalDateTime localDateTime = LocalDateTimeUtil.of((Long) fastTime);
            return LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_TIME_PATTERN);
        }

        return null;
    }

    /**
     * 获取时间值 {@link LocalDateTime}
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 时间值
     */
    private static String getLocalDateTimeValue(Project project, ObjectReference objectReference) {
        ReferenceType referenceType = objectReference.referenceType();
        Field dateField = referenceType.fieldByName("date");
        Field timeField = referenceType.fieldByName("time");
        Value dateValue = objectReference.getValue(dateField);
        Value timeValue = objectReference.getValue(timeField);

        String result = null;
        if (dateValue != null) {
            result = getLocalDateValue(project, (ObjectReference) dateValue);
        }

        if (timeValue != null) {
            String localTimeValue = getLocalTimeValue(project, (ObjectReference) timeValue);
            if (StrUtil.isNotBlank(result)) {
                result += " " + localTimeValue;
            }
        }

        return result;
    }

    /**
     * 获取时间值 {@link LocalDate}
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 时间值
     */
    private static String getLocalDateValue(Project project, ObjectReference objectReference) {
        Integer year = (Integer) getFieldValue(project, objectReference, "year");
        Short month = (Short) getFieldValue(project, objectReference, "month");
        Short day = (Short) getFieldValue(project, objectReference, "day");

        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day).toString();
        }

        return null;
    }

    /**
     * 获取时间值 {@link LocalTime}
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象
     * @return 时间值
     */
    private static String getLocalTimeValue(Project project, ObjectReference objectReference) {
        Byte hour = (Byte) getFieldValue(project, objectReference, "hour");
        Byte minute = (Byte) getFieldValue(project, objectReference, "minute");
        Byte second = (Byte) getFieldValue(project, objectReference, "second");
        Integer nano = (Integer) getFieldValue(project, objectReference, "nano");

        if (hour != null && minute != null && second != null) {
            if (nano == null) {
                return LocalTime.of(hour, minute, second).toString();
            } else {
                return LocalTime.of(hour, minute, second, nano).toString();
            }
        }

        return null;
    }


    /**
     * 获取字段值
     *
     * @param project         项目
     * @param objectReference 要检索其具体值的对象。
     * @param fieldName       字段名
     * @return 字段值
     */
    public static Object getFieldValue(Project project, ObjectReference objectReference, String fieldName) {
        ReferenceType referenceType = objectReference.referenceType();
        Field field = referenceType.fieldByName(fieldName);
        Value value = objectReference.getValue(field);
        return getValue(project, value);
    }


    /**
     * 是否为Bean类型
     *
     * @param project     项目
     * @param objectValue 要检索其具体值的对象
     * @return 若为Bean类型，则为true；否则为false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isBeanType(Project project, ObjectReference objectValue) {
        String name = objectValue.referenceType().name();
        PsiClass psiClass = JavaUtil.findClass(project, name);
        if (Objects.isNull(psiClass)) {
            return false;
        }

        PsiClassType classType = PsiTypesUtil.getClassType(psiClass);
        // 引用类型 还需要List、Array类型
        return JavaUtil.isApplicationClsType(classType);
    }

    /**
     * 当前对象是否为List
     *
     * @param objectReference 要检索其具体值的对象
     * @return 若为List，true；否则false
     */
    private static boolean isCollection(ObjectReference objectReference) {
        return isImplementsInterface(objectReference, Iterable.class.getName());
    }

    /**
     * 解析集合类型
     *
     * @param project   项目
     * @param reference 对象
     * @return 集合类型值列表
     */
    private static boolean hasElements(Project project, ObjectReference reference) {
        BooleanValue isEmptyValue = (BooleanValue) invokeMethod(project, reference, "isEmpty");
        return isEmptyValue != null && !isEmptyValue.value();
    }

    /**
     * 当前对象是否为Map
     *
     * @param objectReference 对象引用镜像
     * @return 为Map，true；否则为false
     */
    private static boolean isMap(ObjectReference objectReference) {
        return isImplementsInterface(objectReference, Map.class.getName());
    }

    /**
     * 当前对象是否实现了指定接口
     *
     * @param objectReference 对象引用镜像
     * @param interfaceName   接口全限定名
     * @return 实现了指定接口则为true；否则为false
     */
    private static boolean isImplementsInterface(ObjectReference objectReference, String interfaceName) {
        ReferenceType type = objectReference.referenceType();
        if (type instanceof ClassType) {
            ClassType classType = (ClassType) type;
            List<InterfaceType> interfaceTypes = classType.allInterfaces();
            return interfaceTypes.stream().anyMatch(el -> Objects.equals(interfaceName, el.name()));
        }

        return false;
    }

    /**
     * 对象是否存在字段
     *
     * @param objectValue 对象值
     * @return 存在为true；否则false
     */
    private static boolean hasFields(ObjectReference objectValue) {
        List<Field> fields = getFields(objectValue);
        return CollUtil.isNotEmpty(fields);
    }


    public static Value invokeMethod(Project project, ObjectReference objectValue, String methodName) {
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();
        if (currentSession == null) {
            return null;
        }

        XDebugProcess debugProcess = currentSession.getDebugProcess();
        JavaStackFrame currentStackFrame = (JavaStackFrame) currentSession.getCurrentStackFrame();
        XDebuggerEvaluator evaluator = debugProcess.getEvaluator();
        if (evaluator == null || currentStackFrame == null) {
            return null;
        }

        DebugProcessImpl myDebugProcess = (DebugProcessImpl) ReflectUtil.getFieldValue(evaluator, "myDebugProcess");
        DebuggerContextImpl debuggerContext = myDebugProcess.getDebuggerContext();
        ThreadReferenceProxyImpl threadProxy = debuggerContext.getThreadProxy();
        if (threadProxy == null) {
            return null;
        }

        AtomicReference<Value> reference = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        myDebugProcess.getManagerThread().invoke(new DebuggerContextCommandImpl(myDebugProcess.getDebuggerContext(), currentStackFrame.getStackFrameProxy().threadProxy()) {
            @Override
            public void threadAction(@NotNull SuspendContextImpl suspendContext) {
                ThreadReference threadReference = threadProxy.getThreadReference();

                ReferenceType referenceType = objectValue.referenceType();
                Method method = referenceType.methodsByName(methodName).get(0);

                try {
                    reference.set(objectValue.invokeMethod(threadReference, method, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED));
                    countDownLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return reference.get();
    }

    public static List<ImmutablePair<Value, Value>> invokeMapMethod(Project project, ObjectReference objectValue) {
        List<ImmutablePair<Value, Value>> resultList = new ArrayList<>();
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();
        if (currentSession == null) {
            return resultList;
        }

        XDebugProcess debugProcess = currentSession.getDebugProcess();
        JavaStackFrame currentStackFrame = (JavaStackFrame) currentSession.getCurrentStackFrame();
        XDebuggerEvaluator evaluator = debugProcess.getEvaluator();
        if (evaluator == null || currentStackFrame == null) {
            return resultList;
        }

        DebugProcessImpl myDebugProcess = (DebugProcessImpl) ReflectUtil.getFieldValue(evaluator, "myDebugProcess");
        DebuggerContextImpl debuggerContext = myDebugProcess.getDebuggerContext();
        ThreadReferenceProxyImpl threadProxy = debuggerContext.getThreadProxy();
        if (threadProxy == null) {
            return resultList;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        myDebugProcess.getManagerThread().invoke(new DebuggerContextCommandImpl(myDebugProcess.getDebuggerContext(), currentStackFrame.getStackFrameProxy().threadProxy()) {
            @Override
            public void threadAction(@NotNull SuspendContextImpl suspendContext) {
                ThreadReference threadReference = threadProxy.getThreadReference();
                ReferenceType referenceType = objectValue.referenceType();
                Method method = referenceType.methodsByName("entrySet").get(0);

                try {
                    ObjectReference entrySet = (ObjectReference) objectValue.invokeMethod(threadReference, method, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                    // toArray
                    ReferenceType entrySetReferenceType = entrySet.referenceType();
                    Method toArrayMethod = entrySetReferenceType.methodsByName("toArray").get(0);
                    ArrayReference toArray = (ArrayReference) entrySet.invokeMethod(threadReference, toArrayMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    List<Value> entryList = toArray.getValues();

                    for (Value entryValue : entryList) {
                        ObjectReference entry = (ObjectReference) entryValue;
                        ReferenceType entryReferenceType = entry.referenceType();
                        Method getKeyMethod = entryReferenceType.methodsByName("getKey").get(0);
                        Method getValueMethod = entryReferenceType.methodsByName("getValue").get(0);

                        Value key = entry.invokeMethod(threadReference, getKeyMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        Value value = entry.invokeMethod(threadReference, getValueMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

                        resultList.add(ImmutablePair.of(key, value));
                    }

                    countDownLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return resultList;
    }


    /**
     * 执行表达式以获取字段值
     *
     * @param project        项目
     * @param expressionText 表达式文本
     * @return 字段值列表
     */
    @SuppressWarnings("unused")
    public static List<Value> evaluate(Project project, String expressionText) {
        List<Value> values = new ArrayList<>();
        Class<?> languageClz = JsonAssistantUtil.getClassByName(FileTypes.JAVA.getLanguageQualifiedName());
        if (languageClz == null) {
            return values;
        }

        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XDebugSession currentSession = debuggerManager.getCurrentSession();
        if (currentSession == null) {
            return values;
        }

        XDebugProcess debugProcess = currentSession.getDebugProcess();
        XDebuggerEvaluator evaluator = debugProcess.getEvaluator();
        if (evaluator == null) {
            return values;
        }

        // 看 com/intellij/xdebugger/impl/evaluate/XDebuggerEvaluationDialog.java:344
        XExpression expression = XDebuggerUtil.getInstance().createExpression(expressionText, LanguageHolder.JAVA, null, EvaluationMode.EXPRESSION);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        XDebuggerEvaluator.XEvaluationCallback callback = new XEvaluationCallbackBase() {
            @Override
            public void evaluated(@NotNull XValue result) {
                JavaValue javaValue = (JavaValue) result;
                ValueDescriptorImpl descriptor = javaValue.getDescriptor();
                Value value = descriptor.getValue();
                values.add(value);
                countDownLatch.countDown();
            }

            @Override
            public void errorOccurred(@NotNull String errorMessage) {

            }
        };

        evaluator.evaluate(expression, callback, null);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return values;
    }

    /**
     * 获取对象的非静态字段
     *
     * @param objectReference 对象引用
     * @return 非静态字段列表
     */
    public static List<Field> getFields(ObjectReference objectReference) {
        ReferenceType referenceType = objectReference.referenceType();
        List<Field> fields = referenceType.allFields();
        return fields.stream().filter(el -> !el.isStatic()).collect(Collectors.toList());
    }

    /**
     * 获取选中节点
     *
     * @param path 路径
     * @return 选中节点
     */
    public static XValueNodeImpl getSelectedNode(final TreePath path) {
        Object node = path.getLastPathComponent();
        return node instanceof XValueNodeImpl ? (XValueNodeImpl) node : null;
    }

    /**
     * 获取选中节点的值
     *
     * @param node 选中节点
     * @return 选中节点的值
     */
    public static XValue getSelectedValue(XValueNodeImpl node) {
        return node != null ? node.getValueContainer() : null;
    }

}
