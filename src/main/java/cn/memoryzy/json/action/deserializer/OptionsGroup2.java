package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class OptionsGroup2 extends DefaultActionGroup {

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

        // 使用本身的 actionPerformed 执行
        // presentation.setPerformGroup(true);


    }


    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new FastJsonToggleAction());
        actions.add(new JacksonToggleAction());
        actions.add(new KeepCamelToggleAction());

        return actions.toArray(new AnAction[0]);
    }

}
