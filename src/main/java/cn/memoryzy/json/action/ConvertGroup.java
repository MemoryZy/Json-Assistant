package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ConvertGroup extends DumbAwareBaseActionGroup {

    public ConvertGroup() {
        super(JsonAssistantBundle.message("group.convert.other.formats.text"), JsonAssistantBundle.messageOnSystem("group.convert.other.formats.description"), JsonAssistantIcons.FUNCTION);
        setPopup(true);
        setEnabledInModalContext(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        return new AnAction[]{
                ActionHolder.Convert.TO_XML_ACTION,
                ActionHolder.Convert.TO_YAML_ACTION,
                ActionHolder.Convert.TO_TOML_ACTION,
                ActionHolder.Convert.TO_JSON5_ACTION,
                ActionHolder.Convert.TO_JSON_ACTION,
                ActionHolder.Convert.TO_TYPESCRIPT_ACTION,
                ActionHolder.Convert.TO_PROPERTIES_ACTION,
                ActionHolder.Convert.TO_URL_PARAM_ACTION
        };
    }

}
