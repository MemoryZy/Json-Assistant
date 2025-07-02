package cn.memoryzy.json.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.notification.DonateAction;
import cn.memoryzy.json.action.toolwindow.*;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.ui.JsonAssistantToolWindowComponentProvider;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
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
        toolWindow.setHelpId(UrlType.VIEW.getId());
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ContentManager contentManager = toolWindow.getContentManager();
        ToolWindowEx toolWindowEx = (ToolWindowEx) toolWindow;

        // 主界面
        JsonAssistantToolWindowComponentProvider window = new JsonAssistantToolWindowComponentProvider(project, FileTypeHolder.JSON5);

        // 补充工具窗口的操作栏
        toolWindowEx.setTabActions(createTabActions(contentFactory, toolWindowEx));
        toolWindowEx.setTitleActions(createTitleActions(toolWindowEx));
        toolWindowEx.setAdditionalGearActions(createAdditionalGearActions(toolWindowEx));

        // 创建初始内容页
        Content content = contentFactory.createContent(null, PluginConstant.MAIN_WINDOW_DISPLAY_NAME, false);
        window.setCurrentContent(content);
        content.setComponent(window.createComponent());
        content.setCloseable(false);
        content.setDisposer(window);
        contentManager.addContent(content, 0);

        ApplicationManager.getApplication().invokeLater(() -> {
            // 检查位置
            if (ToolWindowAnchor.RIGHT.equals(toolWindow.getAnchor()) && !toolWindow.isSplitMode()) {
                ToolWindowUtil.moveWindowToRightBottom(toolWindow);
            }

            // 提醒手动保存历史记录
            showManualSaveReminder(project);
        });
    }

    private void showManualSaveReminder(@NotNull Project project) {
        // 一天提示一次，总共3次
        PropertiesComponent component = PropertiesComponent.getInstance();
        String value = component.getValue(PluginConstant.MANUAL_SAVE_HISTORY_REMINDER);

        // 时间戳
        long timestamp = 0;
        // 总次数
        int time = 0;

        if (StrUtil.isNotBlank(value)) {
            String[] split = value.split("-");
            // 时间戳
            timestamp = Long.parseLong(split[0]);
            // 总次数
            time = Integer.parseInt(split[1]);
        }

        // 大于等于3次 或 今天已经提示过
        if (time >= 3 || JsonAssistantUtil.isTimestampToday(timestamp)) return;

        ToolWindowManager.getInstance(project).notifyByBalloon(
                PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID,
                MessageType.INFO,
                JsonAssistantBundle.messageOnSystem("hint.manual.history.content"));

        // 时间戳 + 次数
        component.setValue(PluginConstant.MANUAL_SAVE_HISTORY_REMINDER, System.currentTimeMillis() + "-" + (time + 1));
    }

    /**
     * 构建选项卡右侧的操作栏
     *
     * @return 操作栏
     */
    private AnAction[] createTabActions(ContentFactory contentFactory, ToolWindowEx toolWindowEx) {
        return new NewTabAction[]{new NewTabAction(contentFactory, toolWindowEx)};
    }

    /**
     * 构建标题行的操作栏（最右侧）
     *
     * @return 操作栏
     */
    private List<AnAction> createTitleActions(ToolWindowEx toolWindowEx) {
        return List.of(
                new BackToEditorViewAction(toolWindowEx),
                Separator.create(),
                new UpgradeHintAction(),
                Separator.create(),
                new JsonHistoryAction(toolWindowEx),
                new OpenSettingsAction());
    }

    /**
     * 构建弹出菜单的的操作栏
     *
     * @return 操作栏
     */
    private ActionGroup createAdditionalGearActions(ToolWindowEx toolWindowEx) {
        SimpleActionGroup group = new SimpleActionGroup();
        group.add(Separator.create());
        group.add(new RenameTabAction());
        group.add(new MoveToEditorAction(toolWindowEx));
        group.add(new FloatingWindowAction(toolWindowEx));
        group.add(new EditInNewWindowAction(toolWindowEx));
        group.add(Separator.create());
        group.add(new ManageClipboardDataBlacklistAction(toolWindowEx));
        group.add(Separator.create());
        group.add(new DonateAction(JsonAssistantBundle.messageOnSystem("action.donate.text")));
        group.add(Separator.create());
        return group;
    }


}
