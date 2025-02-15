package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
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
import com.sun.jdi.Value;

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

    public static boolean isObjectNodeWithChildren(Project project, DataContext dataContext) {
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
        // 引用类型
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
}
