package cn.memoryzy.json.actions.child;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.PluginConstant;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.content.BaseLabel;
import com.intellij.ui.content.Content;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class RenameViewAction extends DumbAwareAction implements UpdateInBackground {

    public RenameViewAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.rename.view.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.rename.view.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(e.getProject());
        Content contextContent = getContextContent(e, toolWindow);

        // ToolWindowTabRenameActionBase

        System.out.println();

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ToolWindow toolWindow = e.getDataContext().getData(PlatformDataKeys.TOOL_WINDOW);
        if (toolWindow == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Content content = getContextContent(e, toolWindow);
        e.getPresentation().setEnabledAndVisible(project != null
                && Objects.equals(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID, toolWindow.getId())
                && content != null);
    }

    @Nullable
    private static Content getContextContent(@NotNull AnActionEvent e, @NotNull ToolWindow toolWindow) {
        BaseLabel baseLabel = ObjectUtils.tryCast(e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT), BaseLabel.class);
        Content selectedContent = baseLabel != null ? baseLabel.getContent() : null;
        if (selectedContent == null) {
            selectedContent = toolWindow.getContentManager().getSelectedContent();
        }
        return selectedContent;
    }
}
