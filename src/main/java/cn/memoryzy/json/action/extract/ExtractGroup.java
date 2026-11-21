package cn.memoryzy.json.action.extract;

import cn.memoryzy.json.action.DumbAwareBaseActionGroup;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.ActionHolder;
import cn.memoryzy.json.util.JsonExtractUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 提取分组（仿照 SortGroup 的弹窗分组写法）
 * <p>
 * 子动作：复制光标处的 Json 键 / 值 / 键值对 / 路径，顺序与结构树的右键菜单一致。
 * 整个分组只在「以 JSON 格式打开的编辑器」中展示，普通文本编辑器里不出现。
 *
 * @author Memory
 * @since 2025/11/21
 */
public class ExtractGroup extends DumbAwareBaseActionGroup {

    public ExtractGroup() {
        super(JsonAssistantBundle.message("group.extract.text"),
                JsonAssistantBundle.messageOnSystem("group.extract.description"),
                JsonAssistantIcons.GRAB);
        setPopup(true);
        setEnabledInModalContext(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        return new AnAction[]{
                ActionHolder.Extract.COPY_JSON_KEY_ACTION,
                ActionHolder.Extract.COPY_JSON_VALUE_ACTION,
                ActionHolder.Extract.COPY_JSON_KEY_VALUE_ACTION,
                ActionHolder.Extract.COPY_JSON_PATH_ACTION,
        };
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        // 非 Json 编辑器（例如普通文本、Java 文件）不展示该分组；
        // 光标处没有可提取的 Json 节点时同样不展示，避免出现四个动作全部置灰的空分组
        boolean visible = ReadAction.compute(() -> JsonExtractUtil.isJsonEditor(dataContext)
                && JsonExtractUtil.isExtractable(project, dataContext));
        event.getPresentation().setEnabledAndVisible(visible);
    }

}