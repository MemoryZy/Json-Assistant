package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonAnnotations;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.psi.PsiClass;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


/**
 * @author Memory
 * @since 2024/12/23
 */
public class OptionsGroup extends DefaultActionGroup {

    private final Module module;
    private final DeserializerState deserializerState;

    public OptionsGroup(DeserializerState deserializerState, Module module) {
        super(JsonAssistantBundle.messageOnSystem("dialog.deserialize.options.text"), true);
        this.module = module;
        this.deserializerState = deserializerState;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setIcon(AllIcons.General.Settings);
        // 使用本身的 actionPerformed 执行
        presentation.setPerformGroup(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();
        JBCheckBox fastJsonCheckBox = new JBCheckBox(JsonAssistantBundle.messageOnSystem("action.deserializer.fastJson.text"), deserializerState.fastJsonAnnotation);
        JBCheckBox jacksonCheckBox = new JBCheckBox(JsonAssistantBundle.messageOnSystem("action.deserializer.jackson.text"), deserializerState.jacksonAnnotation);
        JBCheckBox keepCamelCheckBox = new JBCheckBox(JsonAssistantBundle.messageOnSystem("action.deserializer.keepCamel.text"), deserializerState.keepCamelCase);

        // TODO 当系统不存在 FastJson、Jackson 的依赖，那就置灰
        PsiClass jfAnnotation = JavaUtil.findClass(module, JsonAnnotations.FAST_JSON_JSON_FIELD.getValue());
        PsiClass jf2Annotation = JavaUtil.findClass(module, JsonAnnotations.FAST_JSON2_JSON_FIELD.getValue());
        PsiClass jpAnnotation = JavaUtil.findClass(module, JsonAnnotations.JACKSON_JSON_PROPERTY.getValue());



        listModel.addElement(fastJsonCheckBox);
        listModel.addElement(jacksonCheckBox);
        listModel.addElement(keepCamelCheckBox);

        CheckBoxList<JBCheckBox> checkBoxList = new CheckBoxList<>(listModel);

        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(checkBoxList, null)
                .setFocusable(true)
                .setShowShadow(true)
                .setShowBorder(true)
                .setLocateWithinScreenBounds(true)
                .setLocateByContent(true)
                .setNormalWindowLevel(true)
                .addListener(new SaveOptionsListener(listModel))
                .createPopup()
                .showInBestPositionFor(dataContext);
    }

    private class SaveOptionsListener implements JBPopupListener {

        private final DefaultListModel<JCheckBox> listModel;

        public SaveOptionsListener(DefaultListModel<JCheckBox> listModel) {
            this.listModel = listModel;
        }

        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
            // 弹窗关闭后，保存选中/未选中的状态
            JCheckBox fastJsonCheckBox = listModel.getElementAt(0);
            JCheckBox jacksonCheckBox = listModel.getElementAt(1);
            JCheckBox keepCamelCheckBox = listModel.getElementAt(2);

            deserializerState.fastJsonAnnotation = fastJsonCheckBox.isSelected();
            deserializerState.jacksonAnnotation = jacksonCheckBox.isSelected();
            deserializerState.keepCamelCase = keepCamelCheckBox.isSelected();
        }
    }
}
