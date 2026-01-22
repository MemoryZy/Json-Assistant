package cn.memoryzy.json.action;

import cn.memoryzy.json.action.extend.ConvertAllReadableTimeAction;
import cn.memoryzy.json.action.extend.ConvertAllTimestampAction;
import cn.memoryzy.json.action.extend.ExpandAllNestedJsonAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/2/25
 */
public class ExtendGroup extends DumbAwareBaseActionGroup {

    public ExtendGroup() {
        super(JsonAssistantBundle.message("group.extend.text"), JsonAssistantBundle.messageOnSystem("group.extend.description"), JsonAssistantIcons.OPEN);
        setPopup(true);
        setEnabledInModalContext(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(ActionHolder.Extend.EXPAND_ALL_NESTED_JSON_ACTION);
        actions.add(Separator.create());
        actions.add(ActionHolder.Extend.CONVERT_ALL_READABLE_TIME_ACTION);
        actions.add(ActionHolder.Extend.CONVERT_ALL_TIMESTAMP_ACTION);

        if (PlatformUtil.hasJavaEnvironment(getEventProject(e))) {
            // actions.add(Separator.create());
            // actions.add(ActionHolder.FILL_COMMENT_FROM_JAVA_ACTION);
        }

        return actions.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        DataContext dataContext = e.getDataContext();
        e.getPresentation().setEnabledAndVisible(
                ConvertAllReadableTimeAction.containsSpecialType(dataContext)
                        || ConvertAllTimestampAction.containsSpecialType(dataContext)
                        || ExpandAllNestedJsonAction.containsSpecialType(dataContext)
                        // || FillCommentFromJavaAction.hasJavaEnvironment(project)
        );
    }


}
