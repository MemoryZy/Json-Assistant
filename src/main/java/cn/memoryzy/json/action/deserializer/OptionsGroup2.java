package cn.memoryzy.json.action.deserializer;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.KeepingPopupOpenAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class OptionsGroup2 extends DefaultActionGroup implements KeepingPopupOpenAction{

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

        ListSelectionListener[] listSelectionListeners = myList.getListSelectionListeners();


        popup.showInBestPositionFor(dataContext);
        // });


    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new FastJsonToggleAction());
        actions.add(new JacksonToggleAction());
        actions.add(new KeepCamelToggleAction());

        return actions.toArray(new AnAction[0]);
    }


    public class FastJsonToggleAction extends ToggleAction {

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

    public class JacksonToggleAction extends ToggleAction  {

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

    public class KeepCamelToggleAction extends ToggleAction  {

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
