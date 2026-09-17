package cn.memoryzy.json.action.test;

import cn.hutool.core.collection.ListUtil;
import cn.memoryzy.json.ui.dialog.ModifyNodeValueDialog;
import cn.memoryzy.json.ui.panel.CreateWithTemplatesDialogPanel;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2026/1/20
 */
public class UiTestAction extends BaseTestDumbAwareAction {

    @Override
    public String getActionId() {
        return "JsonAssistant.Action.UiTestAction";
    }

    @Override
    public String getActionText() {
        return "UI测试";
    }

    @Override
    public String getParentGroupId() {
        return "HelpMenu";
    }

    @Override
    public Constraints getConstraints() {
        return Constraints.FIRST;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();



        // simulateNewPopup(project);

        modifyNodeValueDialog(project);
    }

    private void modifyNodeValueDialog(Project project) {
        // DialogBuilder dialogBuilder = new DialogBuilder().title("修改节点值");
        //
        // DialogBuilder.CustomizableAction okAction = dialogBuilder.addOkAction();
        // okAction.setText("确定");
        // DialogBuilder.CustomizableAction cancelAction = dialogBuilder.addCancelAction();
        // cancelAction.setText("取消");
        //
        // // 样式
        // ExpandableEditorTextField expandableTextField = new ExpandableEditorTextField(project, Json5Language.INSTANCE);
        // expandableTextField.setText("12");
        // expandableTextField.selectAll();
        //
        // JBCheckBox checkBox = new JBCheckBox("作用于源文件");
        //
        // BorderLayoutPanel rootPanel = new BorderLayoutPanel()
        //         .addToTop(expandableTextField)
        //         .addToLeft(checkBox);
        //
        // dialogBuilder.centerPanel(rootPanel);
        // dialogBuilder.show();
        //
        //
        // MessageDialogBuilder.okCancel()

        new ModifyNodeValueDialog(project, "12").show();


    }

    /**
     * 模拟IDEA新建类时的弹窗
     */
    private void simulateNewPopup(Project project) {
        // 第一个做文本展示，第二个做图标展示，第三个做标识符
        CreateWithTemplatesDialogPanel.TemplatePresentation classTemp = new CreateWithTemplatesDialogPanel.TemplatePresentation("Class", PlatformIcons.CLASS_ICON, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME);

        CreateWithTemplatesDialogPanel.TemplatePresentation inteTemp = new CreateWithTemplatesDialogPanel.TemplatePresentation("Interface", PlatformIcons.INTERFACE_ICON, JavaTemplateUtil.INTERNAL_INTERFACE_TEMPLATE_NAME);
        CreateWithTemplatesDialogPanel.TemplatePresentation recordTemp = new CreateWithTemplatesDialogPanel.TemplatePresentation("Record", PlatformIcons.RECORD_ICON, JavaTemplateUtil.INTERNAL_RECORD_TEMPLATE_NAME);
        List<CreateWithTemplatesDialogPanel.TemplatePresentation> myTemplatesList = ListUtil.list(false, classTemp, inteTemp, recordTemp);

        CreateWithTemplatesDialogPanel contentPanel = new CreateWithTemplatesDialogPanel("Interface", myTemplatesList);
        JBPopup popup = NewItemPopupUtil.createNewItemPopup("起飞", contentPanel, contentPanel.getNameField());
        popup.showCenteredInCurrentWindow(project);
    }

}
