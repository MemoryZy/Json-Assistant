package cn.memoryzy.json.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.AuxiliaryTreeToolWindowComponentProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 管理树结构工具窗口的行为
 *
 * @author Memory
 * @since 2024/12/12
 */
public class AuxiliaryTreeToolWindowManager {

    private final Project project;
    private final ToolWindow toolWindow;

    public AuxiliaryTreeToolWindowManager(Project project) {
        this.project = project;
        this.toolWindow = registerToolWindow(project);
    }

    public static AuxiliaryTreeToolWindowManager getInstance(@NotNull Project project) {
        return project.getService(AuxiliaryTreeToolWindowManager.class);
    }

    public void convertAndShow(JsonWrapper jsonWrapper) {
        // 为其分配一个标签页，用于展示
        AuxiliaryTreeToolWindowComponentProvider provider = new AuxiliaryTreeToolWindowComponentProvider(jsonWrapper);
        // 创建标签页
        createToolWindowContent(provider.createComponent(toolWindow.getComponent()));
        // 展示
        show();
    }


    @SuppressWarnings("deprecation")
    private ToolWindow registerToolWindow(Project project) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow(PluginConstant.AUXILIARY_TREE_TOOLWINDOW_ID);
        if (toolWindow == null) {
            // 这里的parentDisposable无效
            toolWindow = windowManager.registerToolWindow(
                    PluginConstant.AUXILIARY_TREE_TOOLWINDOW_ID,
                    true,
                    ToolWindowAnchor.RIGHT,
                    () -> {},
                    true,
                    true);
        }

        String title = JsonAssistantBundle.message("toolwindow.auxiliary.tree.name");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE);
        return toolWindow;
    }

    private void createToolWindowContent(JComponent component) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();
        int count = contentManager.getContentCount();

        Content content = contentFactory.createContent(component, getDisplayName(count), false);
        content.setCloseable(true);
        content.setDisposer(new AuxiliaryTreeContentDisposer());
        contentManager.addContent(content, count);
        contentManager.setSelectedContent(content, true);
    }

    private String getDisplayName(int contentCount) {
        String concatStr = "";
        if (contentCount > 0) {
            concatStr += " " + (contentCount + 1);
        }

        return PluginConstant.AUXILIARY_TREE_TOOL_WINDOW_DISPLAY_NAME + concatStr;
    }


    private void show() {
        if (!toolWindow.isAvailable()) {
            toolWindow.setAvailable(true);
        }

        // 辅助窗口只允许打开一个，会自动隐藏其他的窗口
        toolWindow.show();
    }

    public Project getProject() {
        return project;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }


    private class AuxiliaryTreeContentDisposer implements Disposable {
        /**
         * 目前此方法用于处理最后一个标签页关闭的情况<br/>
         * 当最后一个标签页关闭，工具窗口暂时隐藏
         */
        @Override
        public void dispose() {
            // 当最后一个标签页关闭，工具窗口暂时隐藏
            ContentManager contentManager = toolWindow.getContentManager();
            int contentCount = contentManager.getContentCount();
            if (contentCount == 0) {
                toolWindow.setAvailable(false);
            }
        }
    }


}
