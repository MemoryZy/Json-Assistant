package cn.memoryzy.json.toolwindow;

import cn.memoryzy.json.action.notification.DonateAction;
import cn.memoryzy.json.action.toolwindow.FloatingWindowAction;
import cn.memoryzy.json.action.toolwindow.RenameTabAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.AuxiliaryTreeToolWindowComponentProvider;
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
 * 管理树结构工具窗口的行为
 *
 * @author Memory
 * @since 2024/12/12
 */
@Service(Service.Level.PROJECT)
public final class AuxiliaryTreeToolWindowManager implements Disposable {

    private final Project project;
    private final ToolWindow toolWindow;

    public AuxiliaryTreeToolWindowManager(Project project) {
        this.project = project;
        this.toolWindow = createToolWindow(project);
    }

    public static AuxiliaryTreeToolWindowManager getInstance(@NotNull Project project) {
        return project.getService(AuxiliaryTreeToolWindowManager.class);
    }

    public void convertAndShow(JsonWrapper jsonWrapper, EditorContext editorContext) {
        // 为其分配一个标签页，用于展示
        AuxiliaryTreeToolWindowComponentProvider provider = new AuxiliaryTreeToolWindowComponentProvider(jsonWrapper, toolWindow.getComponent(), editorContext);

        // 创建标签页
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();
        int count = contentManager.getContentCount();

        String displayName = ToolWindowUtil.generateTagName(contentManager, PluginConstant.AUXILIARY_TREE_WINDOW_DISPLAY_NAME);
        Content content = contentFactory.createContent(provider.createComponent(), displayName, false);
        content.setCloseable(true);
        content.setPreferredFocusableComponent(provider.getPreferredFocusedComponent());
        content.setDisposer(ToolWindowUtil.createAuxWindowContentDisposer(project, toolWindow));
        contentManager.addContent(content, count);
        contentManager.setSelectedContent(content, true);

        // 展示
        show();
    }


    @SuppressWarnings("deprecation")
    private ToolWindow createToolWindow(Project project) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow(PluginConstant.AUXILIARY_TREE_TOOLWINDOW_ID);
        if (toolWindow == null) {
            // 这里的parentDisposable无效
            toolWindow = windowManager.registerToolWindow(
                    PluginConstant.AUXILIARY_TREE_TOOLWINDOW_ID,
                    true,
                    ToolWindowAnchor.RIGHT,
                    this,
                    true,
                    true);
        }

        String title = JsonAssistantBundle.message("toolwindow.auxiliary.tree.name");
        toolWindow.setTitle(title);
        toolWindow.setStripeTitle(title);
        toolWindow.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE_LOGO);
        registerAction((ToolWindowEx) toolWindow);
        return toolWindow;
    }

    private void registerAction(ToolWindowEx toolWindow) {
        // 右键弹出菜单
        SimpleActionGroup group = new SimpleActionGroup();
        group.add(Separator.create());
        group.add(new RenameTabAction());
        group.add(new FloatingWindowAction(toolWindow));
        group.add(Separator.create());
        group.add(new DonateAction(JsonAssistantBundle.messageOnSystem("action.donate.text")));
        group.add(Separator.create());
        toolWindow.setAdditionalGearActions(group);
    }

    private void show() {
        if (!toolWindow.isAvailable()) {
            toolWindow.setAvailable(true);
        }

        // 将 Json 编辑器窗口移至右上角
        ToolWindowUtil.moveWindowToRightTop(ToolWindowUtil.getJsonAssistantWindow(project));

        // 辅助窗口只允许打开一个，会自动隐藏其他的窗口
        toolWindow.show();
    }

    public Project getProject() {
        return project;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    @Override
    public void dispose() {
    }

}
