package cn.memoryzy.json.toolwindows;

import cn.memoryzy.json.actions.child.*;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Json Tool Window
 *
 * @author Memory
 * @since 2024/6/20
 */
public class JsonViewerToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        String title = JsonAssistantBundle.message("json.window.title");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(PlatformUtil.isNewUi()
                ? JsonAssistantIcons.ExpUi.NEW_TOOL_WINDOW_JSON_PATH
                : JsonAssistantIcons.TOOL_WINDOW_JSON_PATH);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();

        ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;
        JsonViewerWindow window = new JsonViewerWindow(project);
        List<AnAction> dumbAwareActions =
                List.of(new JsonStructureOnToolWindowAction(window, toolWindowEx),
                        new JsonPathFilterOnTextFieldAction(window),
                        Separator.create(),
                        new JsonHistoryAction(window, toolWindowEx),
                        new DonateAction(JsonAssistantBundle.messageOnSystem("action.donate.text")));

        SimpleActionGroup group = new SimpleActionGroup();
        group.add(new FloatingWindowAction(toolWindowEx));

        toolWindowEx.setTabActions(new NewTabAction(contentFactory, toolWindowEx));
        toolWindowEx.setTitleActions(dumbAwareActions);
        toolWindowEx.setAdditionalGearActions(group);

        // 用 SimpleToolWindowPanel  CommonActionsManager

        Content content = contentFactory.createContent(window.getRootPanel(), "View", false);
        content.setCloseable(false);
        contentManager.addContent(content);

        // 验证地址可达性
        HyperLinks.verifyReachable();
    }

}
