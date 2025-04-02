package cn.memoryzy.json.action.group;

import cn.memoryzy.json.action.deserializer.FastJson2ToggleAction;
import cn.memoryzy.json.action.deserializer.FastJsonToggleAction;
import cn.memoryzy.json.action.deserializer.JacksonToggleAction;
import cn.memoryzy.json.action.deserializer.KeepCamelToggleAction;
import cn.memoryzy.json.action.deserializer.comment.SwaggerToggleAction;
import cn.memoryzy.json.action.deserializer.comment.SwaggerV3ToggleAction;
import cn.memoryzy.json.action.deserializer.lombok.LombokGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.DependencyConstant;
import cn.memoryzy.json.service.persistent.state.DeserializerState;
import cn.memoryzy.json.util.JavaUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class OptionsGroup extends DefaultActionGroup implements UpdateInBackground {

    private final Module module;
    private final DeserializerState deserializerState;

    public OptionsGroup(Module module, DeserializerState deserializerState) {
        super(JsonAssistantBundle.messageOnSystem("dialog.deserialize.options.text"), true);
        this.module = module;
        this.deserializerState = deserializerState;
        Presentation presentation = getTemplatePresentation();
        presentation.setIcon(AllIcons.General.Settings);

        // com.intellij.openapi.actionSystem.impl.ActionButton.paintDownArrow 渲染图标下标记，表示菜单的意思
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        Separator attributeSeparator = Separator.create(JsonAssistantBundle.messageOnSystem("separator.attribute"));
        Separator json5Separator = Separator.create("JSON5");

        if (JavaUtil.hasLibrary(module, DependencyConstant.LOMBOK_LIB)) {
            actions.add(new LombokGroup(deserializerState));
        }

        actions.add(Separator.create());
        if (JavaUtil.hasJacksonLib(module)) {
            if (!actions.contains(attributeSeparator)) {
                actions.add(attributeSeparator);
            }
            actions.add(new JacksonToggleAction(deserializerState));
        }

        if (JavaUtil.hasFastJsonLib(module)) {
            if (!actions.contains(attributeSeparator)) {
                actions.add(attributeSeparator);
            }
            actions.add(new FastJsonToggleAction(deserializerState));
        }

        if (JavaUtil.hasFastJson2Lib(module)) {
            if (!actions.contains(attributeSeparator)) {
                actions.add(attributeSeparator);
            }
            actions.add(new FastJson2ToggleAction(deserializerState));
        }

        // ------------------------------------------

        actions.add(Separator.create());
        if (JavaUtil.hasSwaggerV3Lib(module)) {
            if (!actions.contains(json5Separator)) {
                actions.add(json5Separator);
            }
            actions.add(new SwaggerV3ToggleAction(deserializerState));
        }

        if (JavaUtil.hasSwaggerLib(module)) {
            if (!actions.contains(json5Separator)) {
                actions.add(json5Separator);
            }
            actions.add(new SwaggerToggleAction(deserializerState));
        }

        actions.add(Separator.create());
        actions.add(new KeepCamelToggleAction(deserializerState));
        return actions.toArray(new AnAction[0]);
    }

}
