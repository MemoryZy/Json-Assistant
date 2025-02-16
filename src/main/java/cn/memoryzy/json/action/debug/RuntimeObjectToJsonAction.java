package cn.memoryzy.json.action.debug;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.JavaDebugUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/2/14
 */
public class RuntimeObjectToJsonAction extends AnAction {

    public RuntimeObjectToJsonAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.serialize.runtime.object.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.serialize.runtime.object.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        DataContext dataContext = e.getDataContext();

        // boolean complexNodeWithChildren = JavaDebugUtil.isComplexNodeWithChildren(project, dataContext);

        JavaDebugUtil.resolveComplexNode(dataContext);


        //
        // DebugProcessEvents debugProcessEvents = new DebugProcessEvents(project);
        // debugProcessEvents.getManagerThread().invoke(new DebuggerCommandImpl() {
        //     @Override
        //     protected void action() {
        //         XDebuggerTree tree = XDebuggerTree.getTree(dataContext);
        //         if (Objects.nonNull(tree)) {
        //             TreePath[] selectionPaths = tree.getSelectionPaths();
        //             // 只允许选择一个节点
        //             if (ArrayUtil.isNotEmpty(selectionPaths) && selectionPaths.length == 1) {
        //                 TreePath path = selectionPaths[0];
        //                 XValueNodeImpl selectedNode = JavaDebugUtil.getSelectedNode(path);
        //                 XValue selectedValue = JavaDebugUtil.getSelectedValue(selectedNode);
        //
        //                 // 判断是否为 Java-Debug 模式
        //                 if (selectedValue instanceof JavaValue) {
        //                     // 获取类全限定名，判断是否为JavaBean，若是，则取该节点下的那些节点（递归）组合成JSON，再创建标签页
        //                     ValueDescriptorImpl descriptor = ((JavaValue) selectedValue).getDescriptor();
        //                     String declaredType = descriptor.getDeclaredType();
        //                     Type type = descriptor.getType();
        //                     Value value = descriptor.getValue();
        //
        //                     if (Objects.nonNull(type) && (value instanceof ObjectReferenceImpl && !(value instanceof StringReferenceImpl))) {
        //                         PsiClass psiClass = JavaUtil.findClass(project, declaredType);
        //                         if (psiClass != null) {
        //                             PsiClassType classType = PsiTypesUtil.getClassType(psiClass);
        //                             // 引用类型
        //                             if (JavaUtil.isApplicationClsType(classType)) {
        //                                 List<? extends TreeNode> children = selectedNode.getChildren();
        //                                 if (CollUtil.isNotEmpty(children)) {
        //                                     // 递归
        //
        //
        //                                 }
        //
        //
        //
        //                                 System.out.println();
        //
        //                             }
        //
        //
        //                             System.out.println();
        //
        //
        //                         }
        //                     }
        //
        //
        //                     System.out.println();
        //                 }
        //
        //
        //             }
        //         }
        //     }
        // });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // e.getPresentation().setEnabledAndVisible(JavaDebugUtil.isComplexNodeWithChildren(e.getProject(), e.getDataContext()));
    }
}
