package cn.memoryzy.json.toolwindows;

import cn.memoryzy.json.actions.child.DonateAction;
import cn.memoryzy.json.actions.child.FloatingWindowAction;
import cn.memoryzy.json.actions.child.JsonStructureOnToolWindowAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constant.HyperLinks;
import cn.memoryzy.json.ui.JsonViewWindow;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
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
public class JsonViewToolWindowFactory implements ToolWindowFactory, DumbAware {

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
        JsonViewWindow window = new JsonViewWindow(project);
        List<AnAction> dumbAwareActions =
                List.of(new FloatingWindowAction(toolWindowEx),
                        new JsonStructureOnToolWindowAction(project, window, toolWindowEx),
                        new DonateAction(JsonAssistantBundle.messageOnSystem("action.donate.text")));

        toolWindow.setTitleActions(dumbAwareActions);
        Content content = contentFactory.createContent(window.getRootPanel(), null, false);
        contentManager.addContent(content);

        // 验证地址可达性
        HyperLinks.verifyReachable();
    }

}
