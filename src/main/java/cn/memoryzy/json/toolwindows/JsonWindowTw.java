package cn.memoryzy.json.toolwindows;

import cn.memoryzy.json.actions.child.JsonStructureAction;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Json Tool Window
 *
 * @author wcp
 * @since 2024/6/20
 */
public class JsonWindowTw implements ToolWindowFactory {

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        String title = JsonAssistantBundle.message("json.window.title");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();

        JsonWindow window = new JsonWindow(project);
        toolWindow.setTitleActions(List.of(new JsonStructureAction(toolWindow, project, window)));

        Content content = contentFactory.createContent(window.getRootPanel(), "Json", false);
        contentManager.addContent(content);
    }

}
