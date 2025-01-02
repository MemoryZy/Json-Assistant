package cn.memoryzy.json.action.deserializer;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.ui.component.FastJson2OptionsCheckBox;
import cn.memoryzy.json.ui.component.FastJsonOptionsCheckBox;
import cn.memoryzy.json.ui.component.JacksonOptionsCheckBox;
import cn.memoryzy.json.ui.component.KeepCamelOptionsCheckBox;
import cn.memoryzy.json.util.JavaUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.KeepingPopupOpenAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class OptionsGroup2 extends DefaultActionGroup implements KeepingPopupOpenAction{

    // TODO 查看 com.intellij.vcs.log.ui.filter.StructureFilterPopupComponent.SelectVisibleRootAction 类的实现，模仿其实现方式，弹出窗口不消失

    private final Module module;
    private final DeserializerState deserializerState;

    public OptionsGroup2(DeserializerState deserializerState, Module module) {
        super(JsonAssistantBundle.messageOnSystem("dialog.deserialize.options.text"), true);
        this.module = module;
        this.deserializerState = deserializerState;
        // setEnabledInModalContext(false);
        Presentation presentation = getTemplatePresentation();
        presentation.setIcon(AllIcons.General.Settings);
        // presentation.setMultipleChoice(true);
        // presentation.setPopupGroup(true);

        presentation.setPerformGroup(true);

        // 使用本身的 actionPerformed 执行
        // presentation.setPerformGroup(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();

        // IdeEventQueue.invokeLater(() -> {

        // JBPopupFactory.getInstance().createComponentPopupBuilder()
        boolean hasFastJsonLib = JavaUtil.hasFastJsonLib(module);
        boolean hasFastJson2Lib = JavaUtil.hasFastJson2Lib(module);
        boolean hasJacksonLib = JavaUtil.hasJacksonLib(module);

        DefaultListModel<JCheckBox> listModel = createListModel(hasFastJsonLib, hasFastJson2Lib, hasJacksonLib);
        CheckBoxList<JBCheckBox> checkBoxList = new CheckBoxList<>(listModel);

        JBPopup popup1 = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(checkBoxList, null)
                .setFocusable(true)
                .setShowShadow(true)
                .setShowBorder(true)
                .setLocateByContent(true)
                .setNormalWindowLevel(true)
                // .addListener(new SaveOptionsListener(listModel))
                .createPopup();

        popup1.showInBestPositionFor(e.getDataContext());


        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(null,
                        this, dataContext, JBPopupFactory.ActionSelectionAid.MNEMONICS, true);

        // popup.setRequestFocus(true);

        // popup.addListSelectionListener(new ListSelectionListener() {
        //     @Override
        //     public void valueChanged(ListSelectionEvent e) {
        //         popup.setRequestFocus(true);
        //     }
        // });


        Runnable disposeCallback = () -> {
            System.out.println("callback...");
        };

        // TODO com.intellij.ui.popup.list.ListPopupImpl.MyMouseListener.mouseReleased 这个地方执行的关闭弹窗
        // TODO 可能还得从 handleSelect 方法入手，看看 KeepingPopupOpenAction 接口

        // popup.handleSelect(false);

        // ReflectUtil.setFieldValue(popup, "myDisposeCallback", disposeCallback);

        JBList myList = (JBList) ReflectUtil.getFieldValue(popup, "myList");

        // myList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ListSelectionListener[] listSelectionListeners = myList.getListSelectionListeners();

        popup.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object source = e.getSource();
                UIManager.repaintComponent((JComponent) source);

                System.out.println("sync list " + source);
            }
        });

        popup.showInBestPositionFor(dataContext);
        // });


    }

    private DefaultListModel<JCheckBox> createListModel(boolean hasFastJsonLib, boolean hasFastJson2Lib, boolean hasJacksonLib) {
        JBCheckBox fastJsonCheckBox = new FastJsonOptionsCheckBox(module, deserializerState);
        JBCheckBox fastJson2CheckBox = new FastJson2OptionsCheckBox(module, deserializerState);
        JBCheckBox jacksonCheckBox = new JacksonOptionsCheckBox(module, deserializerState);
        JBCheckBox keepCamelCheckBox = new KeepCamelOptionsCheckBox(deserializerState);
        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();

        // 存在某种依赖，就添加某个选项
        if (hasFastJsonLib && hasFastJson2Lib) {
            listModel.addElement(fastJsonCheckBox);
            listModel.addElement(fastJson2CheckBox);

            // 限制单选
            fastJsonCheckBox.addItemListener(new OptionsGroup.RestrictedRadioItemListener(fastJsonCheckBox, fastJson2CheckBox));
            fastJson2CheckBox.addItemListener(new OptionsGroup.RestrictedRadioItemListener(fastJsonCheckBox, fastJson2CheckBox));

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


    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new FastJsonToggleAction());
        actions.add(new JacksonToggleAction());
        actions.add(new KeepCamelToggleAction());

        return actions.toArray(new AnAction[0]);
    }


    public class FastJsonToggleAction extends ToggleAction implements KeepingPopupOpenAction{

        private final DeserializerState deserializerState;

        public FastJsonToggleAction() {
            super("fj", "xxx", null);
            this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return deserializerState.fastJsonAnnotation;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            deserializerState.fastJsonAnnotation = state;
        }
    }

    public class JacksonToggleAction extends ToggleAction implements KeepingPopupOpenAction {

        private final DeserializerState deserializerState;

        public JacksonToggleAction() {
            super("jk", "xxx2", null);
            this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return deserializerState.jacksonAnnotation;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            deserializerState.jacksonAnnotation = state;
        }
    }

    public class KeepCamelToggleAction extends ToggleAction implements KeepingPopupOpenAction {

        private final DeserializerState deserializerState;

        public KeepCamelToggleAction() {
            super("kp", "xxx3", null);
            this.deserializerState = JsonAssistantPersistentState.getInstance().deserializerState;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return deserializerState.keepCamelCase;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            deserializerState.keepCamelCase = state;
        }
    }

}
