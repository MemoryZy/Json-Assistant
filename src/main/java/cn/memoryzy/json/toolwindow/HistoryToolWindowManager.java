package cn.memoryzy.json.toolwindow;

import cn.memoryzy.json.action.notification.DonateAction;
import cn.memoryzy.json.action.toolwindow.FloatingWindowAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/26
 */
@Service(Service.Level.PROJECT)
public final class HistoryToolWindowManager implements Disposable {

    private final Project project;
    private final ToolWindow toolWindow;

    public HistoryToolWindowManager(Project project) {
        this.project = project;
        this.toolWindow = createToolWindow(project);
    }

    public static HistoryToolWindowManager getInstance(@NotNull Project project) {
        return project.getService(HistoryToolWindowManager.class);
    }

    @SuppressWarnings("deprecation")
    private ToolWindow createToolWindow(Project project) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow(PluginConstant.HISTORY_TOOLWINDOW_ID);
        if (toolWindow == null) {
            // 这里的parentDisposable无效
            toolWindow = windowManager.registerToolWindow(
                    PluginConstant.HISTORY_TOOLWINDOW_ID,
                    true,
                    ToolWindowAnchor.RIGHT,
                    this,
                    true,
                    true);
        }

        String title = JsonAssistantBundle.message("toolwindow.history.name");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE_LOGO);
        // 右键弹出菜单
        registerAction((ToolWindowEx) toolWindow);
        return toolWindow;
    }

    private void registerAction(ToolWindowEx toolWindow) {
        // 右键弹出菜单
        SimpleActionGroup group = new SimpleActionGroup();
        group.add(Separator.create());
        group.add(new FloatingWindowAction(toolWindow));
        group.add(Separator.create());
        group.add(new DonateAction(JsonAssistantBundle.messageOnSystem("action.donate.text")));
        group.add(Separator.create());
        // 在203、213等低版本ide中，setAdditionalGearActions方法是属于ToolWindowEx的，单纯用ToolWindow会出错
        toolWindow.setAdditionalGearActions(group);
    }

    public void convertAndShow() {
        // 创建标签页
        createToolWindowContent();
        // 展示
        show();
    }

    private void show() {
        if (!toolWindow.isAvailable()) {
            toolWindow.setAvailable(true);
        }

        // 将 Json 编辑器窗口移至右上角
        ToolWindowUtil.moveWindowToRightTop(ToolWindowUtil.getJsonAssistantToolWindow(project));

        // 辅助窗口只允许打开一个，会自动隐藏其他的窗口
        toolWindow.show();
    }

    private void createToolWindowContent() {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();

        HistoryToolWindowComponentProvider provider = new HistoryToolWindowComponentProvider(project);
        Content content = contentFactory.createContent(provider.createComponent(), "", false);
        content.setCloseable(false);
        content.setDisposer(provider);
        contentManager.addContent(content, 0);
        contentManager.setSelectedContent(content, true);
    }

    @Override
    public void dispose() {
    }
}
