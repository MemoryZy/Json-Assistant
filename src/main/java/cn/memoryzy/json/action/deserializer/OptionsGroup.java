package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DependencyConstant;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.ui.component.*;
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
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


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

        // com.intellij.openapi.actionSystem.impl.ActionButton.paintDownArrow 渲染图标下标记，表示菜单的意思
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean hasFastJsonLib = JavaUtil.hasFastJsonLib(module);
        boolean hasFastJson2Lib = JavaUtil.hasFastJson2Lib(module);
        boolean hasJacksonLib = JavaUtil.hasJacksonLib(module);

        DefaultListModel<JCheckBox> listModel = createListModel(hasFastJsonLib, hasFastJson2Lib, hasJacksonLib);
        CheckBoxList<JBCheckBox> checkBoxList = new CheckBoxList<>(listModel);

        JBPopupFactory.getInstance()
                .createComponentPopupBuilder(checkBoxList, null)
                .setFocusable(true)
                .setShowShadow(true)
                .setShowBorder(true)
                .setLocateByContent(true)
                .setNormalWindowLevel(true)
                .addListener(new SaveOptionsListener(listModel))
                .createPopup()
                .showInBestPositionFor(e.getDataContext());
    }

    @Override
    public boolean canBePerformed(@NotNull DataContext context) {
        // 使用本身的 actionPerformed 执行
        return true;
    }

    private DefaultListModel<JCheckBox> createListModel(boolean hasFastJsonLib, boolean hasFastJson2Lib, boolean hasJacksonLib) {
        JBCheckBox dataCheckBox = new DataLombokOptionsCheckBox(module, deserializerState);
        JBCheckBox accessorsChainCheckBox = new AccessorsChainLombokOptionsCheckBox(module, deserializerState);
        JBCheckBox getterCheckBox = new GetterLombokOptionsCheckBox(module, deserializerState);
        JBCheckBox setterCheckBox = new SetterLombokOptionsCheckBox(module, deserializerState);

        JBCheckBox fastJsonCheckBox = new FastJsonOptionsCheckBox(module, deserializerState);
        JBCheckBox fastJson2CheckBox = new FastJson2OptionsCheckBox(module, deserializerState);
        JBCheckBox jacksonCheckBox = new JacksonOptionsCheckBox(module, deserializerState);
        JBCheckBox keepCamelCheckBox = new KeepCamelOptionsCheckBox(deserializerState);
        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();

        if (JavaUtil.hasLibrary(module, DependencyConstant.LOMBOK_LIB)) {
            listModel.addElement(dataCheckBox);
            listModel.addElement(accessorsChainCheckBox);
            listModel.addElement(getterCheckBox);
            listModel.addElement(setterCheckBox);
        }

        // 存在某种依赖，就添加某个选项
        if (hasFastJsonLib && hasFastJson2Lib) {
            listModel.addElement(fastJsonCheckBox);
            listModel.addElement(fastJson2CheckBox);

            // 限制单选
            fastJsonCheckBox.addItemListener(new RestrictedRadioItemListener(fastJsonCheckBox, fastJson2CheckBox));
            fastJson2CheckBox.addItemListener(new RestrictedRadioItemListener(fastJsonCheckBox, fastJson2CheckBox));

        } else if (hasFastJsonLib) {
            listModel.addElement(fastJsonCheckBox);

        } else if (hasFastJson2Lib) {
            listModel.addElement(fastJson2CheckBox);
        }

        if (hasJacksonLib) {
            listModel.addElement(jacksonCheckBox);
        }

        listModel.addElement(keepCamelCheckBox);
        return listModel;
    }

    public static class RestrictedRadioItemListener implements ItemListener {
        private final JBCheckBox fastJsonCheckBox;
        private final JBCheckBox fastJson2CheckBox;

        public RestrictedRadioItemListener(JBCheckBox fastJsonCheckBox, JBCheckBox fastJson2CheckBox) {
            this.fastJsonCheckBox = fastJsonCheckBox;
            this.fastJson2CheckBox = fastJson2CheckBox;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            Object item = e.getItem();
            int stateChange = e.getStateChange();
            if (item instanceof FastJsonOptionsCheckBox) {
                // fastjson
                if (stateChange == ItemEvent.SELECTED) {
                    // 当fastjson点击选中后，去除fastjson2的选中
                    fastJson2CheckBox.setSelected(false);
                }

            } else {
                // fastjson2
                if (stateChange == ItemEvent.SELECTED) {
                    // 当fastjson点击选中后，去除fastjson2的选中
                    fastJsonCheckBox.setSelected(false);
                }
            }
        }
    }


    private static class SaveOptionsListener implements JBPopupListener {

        private final DefaultListModel<JCheckBox> listModel;

        public SaveOptionsListener(DefaultListModel<JCheckBox> listModel) {
            this.listModel = listModel;
        }

        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
            // 弹窗关闭后，保存选中/未选中的状态
            for (int i = 0; i < listModel.size(); i++) {
                JCheckBox checkBox = listModel.getElementAt(i);
                ((OptionsCheckBox) checkBox).performed();
            }
        }
    }
}
