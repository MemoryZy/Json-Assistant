package cn.memoryzy.json.toolwindow;

import cn.memoryzy.json.action.DonateAction;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
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
public class JsonAssistantToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        String title = JsonAssistantBundle.message("setting.display.name");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(JsonAssistantIcons.ToolWindow.LOGO);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();
        ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;

        // 主界面
        JsonAssistantToolWindowComponentProvider window = new JsonAssistantToolWindowComponentProvider(project, FileTypeHolder.JSON, true);

        // 选项卡旁
        AnAction[] tabActions = {new NewTabAction(contentFactory, toolWindowEx)};
        // 标题行
        List<AnAction> titleActions = List.of(new JsonHistoryAction(toolWindowEx), new OpenSettingsAction(toolWindowEx));

        // 右键弹出菜单
        SimpleActionGroup group = new SimpleActionGroup();
        group.add(Separator.create());
        group.add(new RenameTabAction());
        group.add(new MoveToEditorAction(toolWindowEx));
        group.add(new FloatingWindowAction(toolWindowEx));
        group.add(new EditInNewWindowAction(toolWindowEx));
        group.add(Separator.create());
        group.add(new DonateAction(JsonAssistantBundle.messageOnSystem("help.tooltip.donate.action.description")));
        group.add(Separator.create());

        toolWindowEx.setTabActions(tabActions);
        toolWindowEx.setTitleActions(titleActions);
        toolWindowEx.setAdditionalGearActions(group);

        Content content = contentFactory.createContent(window.createRootPanel(), PluginConstant.JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME, false);
        content.setCloseable(false);
        contentManager.addContent(content, 0);

        // 验证地址可达性
        Urls.verifyReachable();
    }

}
