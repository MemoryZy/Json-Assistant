package cn.memoryzy.json.toolwindows;

import cn.memoryzy.json.actions.child.NewTabAction;
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

/**
 * @author Memory
 * @since 2024/8/20
 */
public class TestTwF implements ToolWindowFactory, DumbAware {

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        String title = "aaaa";
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(JsonAssistantIcons.BOOK);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();

        ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;
        toolWindowEx.setTabActions(new NewTabAction(contentFactory, toolWindowEx));

        TestPanel testPanel = new TestPanel(project);
        // 用 SimpleToolWindowPanel

        Content content = contentFactory.createContent(testPanel.getComponent(), "Json", false);
        content.setCloseable(false);
        contentManager.addContent(content);
    }
}
