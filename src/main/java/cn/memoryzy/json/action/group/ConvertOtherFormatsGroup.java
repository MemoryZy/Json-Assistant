package cn.memoryzy.json.action.group;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2024/8/3
 */
public class ConvertOtherFormatsGroup extends DefaultActionGroup implements DumbAware {

    public ConvertOtherFormatsGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation templatePresentation = getTemplatePresentation();
        templatePresentation.setText(JsonAssistantBundle.message("group.convert.other.formats.text"));
        templatePresentation.setDescription(JsonAssistantBundle.messageOnSystem("group.convert.other.formats.description"));
        templatePresentation.setIcon(JsonAssistantIcons.FUNCTION);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        return new AnAction[]{
                ActionHolder.TO_XML_ACTION,
                ActionHolder.TO_YAML_ACTION,
                ActionHolder.TO_TOML_ACTION,
                ActionHolder.TO_URL_PARAM_ACTION
        };
    }

}
