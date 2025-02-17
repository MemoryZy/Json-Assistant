package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.MessageTreeNode;
import com.intellij.xdebugger.impl.ui.tree.nodes.XEvaluationCallbackBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class JavaDebugUtil {

    /**
     * 判断DataContext中选中的节点是否为对象或者含有子元素的列表。
     *
     * @param dataContext 当前数据上下文环境，包含了当前操作需要的所有信息。
     * @return 如果选中的节点是对象或含有子元素的列表，则返回true；否则返回false。
     */
    public static boolean isObjectOrListWithChildren(DataContext dataContext) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
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
            return CollUtil.isNotEmpty(resolveCollectionOrMapChildren(selectedNode));

        } else {
            // 拒绝包装类型及JDK内部类型
            if (!isBeanType(project, objectValue)) {
                return false;
            }

            return CollUtil.isNotEmpty(resolveObject(objectValue));
        }
    }


    /**
     * 处理复杂节点（对象、集合、Map、数组）
     *
     * @param dataContext 数据上下文
     * @return 根据节点类型返回对应类型值
     */
    public static Object resolveObjectReference(DataContext dataContext) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
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
        return (value instanceof ObjectReference) && !(value instanceof StringReference) ? getValue(project, selectedNode, value) : null;
    }


    /**
     * 根据提供的项目、父节点和值对象，检索并返回相应类型的值。
     *
     * @param project    当前项目上下文，用于获取必要的环境信息。
     * @param parentNode 父节点，通常用于调试器树结构中的导航。
     * @param value      要检索其具体值的对象。
     * @return 返回值的具体表示形式，可能是原始类型、字符串、数组、集合、映射或普通对象。
     */
    public static Object getValue(Project project, XValueNodeImpl parentNode, Value value) {
        if (value == null) {
            return null;
        }

        if (value instanceof PrimitiveValue) {
            return getPrimitiveValue((PrimitiveValue) value);

        } else if (value instanceof StringReference) {
            return getStringValue((StringReference) value);

        } else if (value instanceof ArrayReference) {
            return getArrayValue(project, parentNode, (ArrayReference) value);

        } else {
            ObjectReference reference = (ObjectReference) value;
            if (isCollection(reference)) {
                return getCollectionValue(project, parentNode);

            } else if (isMap(reference)) {
                return getMapValue(reference);

            } else {
                return getObjectValue(project, parentNode, reference);
            }
        }
    }


    /**
     * 获取Map对象值
     *
     * @param reference Map对象引用
     * @return Map对象值
     */
    private static Object getMapValue(ObjectReference reference) {
        Map<String, Object> resultMap = new LinkedHashMap<>();


        return null;
    }

    /**
     * 获取集合值
     *
     * @param parentNode 父节点
     * @return 集合值
     */
    private static List<Object> getCollectionValue(Project project, XValueNodeImpl parentNode) {
        List<Object> resultList = new ArrayList<>();
        List<Value> childrenValues = resolveCollectionOrMapChildren(parentNode);
        for (Value childrenValue : childrenValues) {
            resultList.add(getValue(project, parentNode, childrenValue));
        }

        return resultList;
    }

    /**
     * 获取数组值
     *
     * @param parentNode 父节点
     * @return 数组值
     */
    public static List<Object> getArrayValue(Project project, XValueNodeImpl parentNode, ArrayReference arrayReference) {
        List<Object> resultList = new ArrayList<>();
        List<Value> childrenValues = arrayReference.getValues();
        for (Value childrenValue : childrenValues) {
            resultList.add(getValue(project, parentNode, childrenValue));
        }

        return resultList;
    }


    /**
     * 获取字符串值
     *
     * @param stringReference 字符串类型
     * @return 字符串值
     */
    public static String getStringValue(StringReference stringReference) {
        return stringReference.value();
    }

    /**
     * 获取基本类型值
     *
     * @param primitiveValue 基本类型
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
     * @param objectReference 对象类型
     * @return 对象类型值
     */
    public static Object getObjectValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
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
                return getFieldValue(project, parentNode, objectReference, "value");
            case "java.lang.String":
                return getStringValue((StringReference) objectReference);
            case "java.util.Date":
                return getDateValue(project, parentNode, objectReference);
            case "java.sql.Time":
                return getTimeValue(project, parentNode, objectReference);
            case "java.time.LocalDateTime":
                return getLocalDateTimeValue(project, parentNode, objectReference);
            case "java.time.LocalDate":
                return getLocalDateValue(project, parentNode, objectReference);
            case "java.time.LocalTime":
                return getLocalTimeValue(project, parentNode, objectReference);
            case "java.math.BigDecimal":
                return getBigDecimalValue(parentNode, objectReference);
            default:
                return getBeanValue(project, parentNode, objectReference);
        }
    }


    /**
     * 获取Bean类型所有属性值列表
     *
     * @param project         项目
     * @param parentNode      父节点
     * @param objectReference 对象引用
     * @return 类型属性值列表
     */
    private static Object getBeanValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        if (!isBeanType(project, objectReference)) {
            return null;
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<Field> fields = getFields(objectReference);
        for (Field field : fields) {
            Value value = objectReference.getValue(field);
            resultMap.put(field.name(), getValue(project, parentNode, value));
        }

        return resultMap;
    }


    /**
     * 获取BigDecimal值
     *
     * @param parentNode
     * @param objectReference 对象引用
     * @return BigDecimal值
     */
    private static BigDecimal getBigDecimalValue(XValueNodeImpl parentNode, ObjectReference objectReference) {
        List<? extends TreeNode> children = parentNode.getChildren();
        if (CollUtil.isEmpty(children) || hasMessageNode(children)) {
            return null;
        }

        for (TreeNode child : children) {
            XValueNodeImpl childValueNode = (XValueNodeImpl) child;
            XValue selectedValue = getSelectedValue(childValueNode);
            ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
            Value value = descriptor.getValue();
            if (Objects.equals(objectReference, value)) {

                System.out.println();
            }
        }


        ReferenceType referenceType = objectReference.referenceType();
        List<Field> fields = referenceType.allFields();




        return null;
    }


    /**
     * 获取时间值 {@link java.util.Date}
     *
     * @param project         项目
     * @param parentNode      父节点
     * @param objectReference 对象值
     * @return 时间值
     */
    public static String getDateValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        Object fastTime = getFieldValue(project, parentNode, objectReference, "fastTime");
        if (fastTime instanceof Long) {
            long timestamp = (Long) fastTime;
            return formatDateBasedOnTimestampDetails(timestamp);
        }

        return null;
    }

    private static Object getTimeValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        Object fastTime = getFieldValue(project, parentNode, objectReference, "fastTime");
        if (fastTime instanceof Long) {
            LocalDateTime localDateTime = LocalDateTimeUtil.of((Long) fastTime);
            return LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_TIME_PATTERN);
        }

        return null;
    }

    private static String getLocalDateTimeValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        ReferenceType referenceType = objectReference.referenceType();
        Field dateField = referenceType.fieldByName("date");
        Field timeField = referenceType.fieldByName("time");
        Value dateValue = objectReference.getValue(dateField);
        Value timeValue = objectReference.getValue(timeField);

        String result = null;
        if (dateValue != null) {
            result = getLocalDateValue(project, parentNode, (ObjectReference) dateValue);
        }

        if (timeValue != null) {
            String localTimeValue = getLocalTimeValue(project, parentNode, (ObjectReference) timeValue);
            if (StrUtil.isNotBlank(result)) {
                result += " " + localTimeValue;
            }
        }

        return result;
    }

    private static String getLocalDateValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        Integer year = (Integer) getFieldValue(project, parentNode, objectReference, "year");
        Short month = (Short) getFieldValue(project, parentNode, objectReference, "month");
        Short day = (Short) getFieldValue(project, parentNode, objectReference, "day");

        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day).toString();
        }

        return null;
    }

    private static String getLocalTimeValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference) {
        Byte hour = (Byte) getFieldValue(project, parentNode, objectReference, "hour");
        Byte minute = (Byte) getFieldValue(project, parentNode, objectReference, "minute");
        Byte second = (Byte) getFieldValue(project, parentNode, objectReference, "second");
        Integer nano = (Integer) getFieldValue(project, parentNode, objectReference, "nano");

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
     * 根据时间戳的详细信息（时、分、秒、毫秒）选择合适的日期时间格式并返回格式化后的字符串。
     *
     * @param timestamp 时间戳（单位：毫秒）
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateBasedOnTimestampDetails(long timestamp) {
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
        } else if (second > 0) {
            // 如果秒部分存在值，则包含秒的格式化表达式
            format = DatePattern.NORM_DATETIME_PATTERN;
        } else if (minute > 0 || hour > 0) {
            // 如果分钟或小时部分存在值，则包含时和分的格式化表达式
            format = DatePattern.NORM_DATETIME_MINUTE_PATTERN;
        } else {
            // 默认情况下仅显示日期
            format = DatePattern.NORM_DATE_PATTERN;
        }

        return LocalDateTimeUtil.format(localDateTime, format);
    }

    /**
     * 获取字段值
     *
     * @param project         项目
     * @param parentNode      父节点
     * @param objectReference 对象引用
     * @param fieldName       字段名
     * @return 字段值
     */
    public static Object getFieldValue(Project project, XValueNodeImpl parentNode, ObjectReference objectReference, String fieldName) {
        ReferenceType referenceType = objectReference.referenceType();
        Field field = referenceType.fieldByName(fieldName);
        Value value = objectReference.getValue(field);
        return getValue(project, parentNode, value);
    }


    /**
     * 是否为Bean类型
     *
     * @param project     项目
     * @param objectValue 对象值
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
     * @param objectReference 对象引用镜像
     * @return 为List，true；否则为false
     */
    private static boolean isCollection(ObjectReference objectReference) {
        return isImplementsInterface(objectReference, Iterable.class.getName());
    }

    /**
     * 解析集合类型
     * <pre>
     * 集合类型存在一个问题，那就是可能会存在自定义实现的类型，这样的话必须要 invokeMethod("toArray") 才能获取到值，这样也不好。索性直接获取节点。
     * </pre>
     *
     * @param selectedNode 选中节点
     * @return 集合类型值列表
     */
    private static List<Value> resolveCollectionOrMapChildren(XValueNodeImpl selectedNode) {
        List<Value> elements = new ArrayList<>();
        List<? extends TreeNode> children = selectedNode.getChildren();
        if (CollUtil.isEmpty(children) || hasMessageNode(children)) {
            return elements;
        }

        for (TreeNode child : children) {
            XValue selectedValue = getSelectedValue((XValueNodeImpl) child);
            ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
            elements.add(descriptor.getValue());
        }

        return elements;
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
     * 获取对象字段值列表
     *
     * @param objectValue 对象值
     * @return 字段值列表，若无则为空集合
     */
    private static List<Value> resolveObject(ObjectReference objectValue) {
        List<Value> elements = new ArrayList<>();
        List<Field> fields = getFields(objectValue);
        for (Field field : fields) {
            elements.add(objectValue.getValue(field));
        }

        return elements;
    }


    /**
     * 执行表达式以获取字段值
     *
     * @param project        项目
     * @param expressionText 表达式文本
     * @return 字段值列表
     */
    public static List<Value> evaluate(Project project, String expressionText) {
        List<Value> values = new ArrayList<>();
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
        XExpression expression = XDebuggerUtil.getInstance().createExpression(expressionText, JavaLanguage.INSTANCE, null, EvaluationMode.EXPRESSION);

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

    /**
     * 判断是否有消息节点
     *
     * @param nodes 节点列表
     * @return 是否有消息节点
     */
    public static boolean hasMessageNode(List<? extends TreeNode> nodes) {
        return nodes.stream().anyMatch(el -> el instanceof MessageTreeNode);
    }

}
