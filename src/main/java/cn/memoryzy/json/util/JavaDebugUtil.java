package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.jdi.ObjectReferenceImpl;
import com.jetbrains.jdi.StringReferenceImpl;
import com.sun.jdi.*;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class JavaDebugUtil {

    public static XValueNodeImpl getSelectedNode(final TreePath path) {
        Object node = path.getLastPathComponent();
        return node instanceof XValueNodeImpl ? (XValueNodeImpl) node : null;
    }

    public static XValue getSelectedValue(XValueNodeImpl node) {
        return node != null ? node.getValueContainer() : null;
    }

    public static boolean isComplexNodeWithChildren(Project project, DataContext dataContext) {
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
        XValueNodeImpl selectedNode = JavaDebugUtil.getSelectedNode(path);
        XValue selectedValue = JavaDebugUtil.getSelectedValue(selectedNode);

        // 判断是否为 Java-Debug 模式
        if (!(selectedValue instanceof JavaValue)) {
            return false;
        }

        // 获取类全限定名，判断是否为JavaBean，若是，则取该节点下的那些节点（递归）组合成JSON，再创建标签页
        ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
        Value value = descriptor.getValue();

        if (Objects.isNull(descriptor.getType()) || value instanceof StringReferenceImpl || !(value instanceof ObjectReferenceImpl)) {
            return false;
        }

        PsiClass psiClass = JavaUtil.findClass(project, descriptor.getDeclaredType());
        if (Objects.isNull(psiClass)) {
            return false;
        }

        PsiClassType classType = PsiTypesUtil.getClassType(psiClass);
        // 引用类型 还需要List、Array类型
        if (!JavaUtil.isApplicationClsType(classType)) {
            return false;
        }

        List<? extends TreeNode> children = selectedNode.getChildren();
        if (children.size() == 1) {
            XValueNodeImpl treeNode = (XValueNodeImpl) children.get(0);
            String rawValue = treeNode.getRawValue();
            if (StrUtil.equals("Class has no fields", rawValue)) {
                return false;
            }
        }

        return CollUtil.isNotEmpty(children);
    }

    /**
     * 处理复杂节点（对象、集合、Map、数组）
     *
     * @param dataContext 数据上下文
     * @return 根据节点类型返回对应类型值
     */
    public static Object resolveComplexNode(DataContext dataContext) {
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
        XValueNodeImpl selectedNode = JavaDebugUtil.getSelectedNode(path);
        XValue selectedValue = JavaDebugUtil.getSelectedValue(selectedNode);

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

        if (value instanceof ArrayReference) {
            // 数组类型
            return resolveArrayNode(selectedNode, (ArrayReference) value);
        } else if (value instanceof ObjectReference && !(value instanceof StringReference)) {
            // 对象类型（包括集合）
            return resolveObjectNode((ObjectReference) value);
        }

        return null;

        // if (value instanceof StringReferenceImpl || !(value instanceof ObjectReferenceImpl)) {
        //     return false;
        // }
        //
        // PsiClass psiClass = JavaUtil.findClass(project, descriptor.getDeclaredType());
        // if (Objects.isNull(psiClass)) {
        //     return false;
        // }
        //
        // PsiClassType classType = PsiTypesUtil.getClassType(psiClass);
        // // 引用类型 还需要List、Array类型
        // if (!JavaUtil.isApplicationClsType(classType)) {
        //     return false;
        // }
        //
        // List<? extends TreeNode> children = selectedNode.getChildren();
        // if (children.size() == 1) {
        //     XValueNodeImpl treeNode = (XValueNodeImpl) children.get(0);
        //     String rawValue = treeNode.getRawValue();
        //     if (StrUtil.equals("Class has no fields", rawValue)) {
        //         return false;
        //     }
        // }
        //
        // return CollUtil.isNotEmpty(children);

    }

    private static Object resolveObjectNode(ObjectReference objectReference) {
        ReferenceType referenceType = objectReference.referenceType();




        //
        List<Field> fields = referenceType.allFields();

        for (Field field : fields) {

            String name = field.name();
            Value value = objectReference.getValue(field);

            if (value instanceof ObjectReference && !(value instanceof StringReference)) {
                ObjectReference nestedObjectReference = (ObjectReference) value;
                ReferenceType nestedReferenceType = nestedObjectReference.referenceType();
                String typeName = nestedReferenceType.name();

                if (typeName.startsWith("java.lang.") || typeName.startsWith("java.math.")) {
                    if ("java.math.BigDecimal".equals(typeName)) {
                        List<Field> fields1 = nestedReferenceType.allFields();

                        for (Field field1 : fields1) {
                            Value value1 = nestedObjectReference.getValue(field1);

                            System.out.println();
                        }

                    }


                    Field field1 = nestedReferenceType.fieldByName("value");

                    Value value1 = nestedObjectReference.getValue(field1);

                    System.out.println();

                }

            }

            System.out.println();

        }

        return null;
    }

    private static void resolveStringNode() {

    }

    private static List<Object> resolveArrayNode(XValueNodeImpl selectedNode, ArrayReference arrayReference) {
        int length = arrayReference.length();
        if (length == 0) {
            return List.of();
        }

        List<Value> values = arrayReference.getValues();

        // IntegerValue

        return null;
    }

}
