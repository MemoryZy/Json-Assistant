package cn.memoryzy.json.action.group;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.enums.JsonValueHandleType;
import cn.memoryzy.json.util.JsonValueHandler;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/2/25
 */
public class ExtendGroup extends DefaultActionGroup implements DumbAware, UpdateInBackground {

    public ExtendGroup() {
        super();
        setPopup(true);
        setEnabledInModalContext(true);
        Presentation templatePresentation = getTemplatePresentation();
        templatePresentation.setText(JsonAssistantBundle.message("group.extend.text"));
        templatePresentation.setDescription(JsonAssistantBundle.messageOnSystem("group.extend.description"));
        templatePresentation.setIcon(JsonAssistantIcons.OPEN);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{
                ActionHolder.EXPAND_ALL_NESTED_JSON_ACTION,
                ActionHolder.CONVERT_ALL_TIMESTAMP_ACTION,
        };
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        e.getPresentation().setEnabledAndVisible(
                JsonValueHandler.containsSpecialType(dataContext, JsonValueHandleType.TIMESTAMP)
                        || JsonValueHandler.containsSpecialType(dataContext, JsonValueHandleType.NESTED_JSON));
    }
}
