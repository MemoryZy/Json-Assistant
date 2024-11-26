package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.action.notification.IgnoreAction;
import cn.memoryzy.json.action.notification.RecoverAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.ui.dialog.JsonHistoryChooser;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.google.common.collect.Lists;
import com.intellij.conversion.ComponentManagerSettings;
import com.intellij.ide.HelpTooltip;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction implements CustomComponentAction {

    private final ToolWindowEx toolWindow;

    public JsonHistoryAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.history.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.HISTORY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt H"), toolWindow.getComponent());
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                // noinspection DialogTitleCapitalization
                new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setShortcut(getShortcut())
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.history.description"))
                        .installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        new JsonHistoryChooser(project, toolWindow).show();
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+H";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
    }


    /**
     * 兼容旧版本的历史记录数据
     *
     * @param project 项目
     */
    public static void compatibilityHistory(Project project) {
        // 历史记录检测
        ApplicationManager.getApplication().invokeLater(() -> {
            ComponentManagerSettings projectDataManagerSettings = PlatformUtil.getProjectDataManagerSettings(project);
            // 获取之前版本历史记录的 State Key
            Element historyElement = projectDataManagerSettings.getComponentElement("JsonAssistantJsonHistory");
            // 获取属性值
            String oriHistory = (historyElement == null) ? null : historyElement.getAttributeValue("historyList");

            if (StrUtil.isBlank(oriHistory) && JsonUtil.isJsonArray(oriHistory)) {
                return;
            }

            ArrayWrapper array = JsonUtil.parseArray(oriHistory);
            if (array.isEmpty()) {
                return;
            }

            for (Object data : array) {
                ObjectWrapper objectWrapper = JsonUtil.parseObject((String) data);

                System.out.println();
            }

            // 与当前版本存在的历史记录做匹配，看看是否有匹配项，有的话就不计入

            // 添加进去后，删除旧版本记录

            Notifications.showFullNotification(
                    "Json Assistant",
                    JsonAssistantBundle.messageOnSystem("notification.recover.content", 90),
                    NotificationType.INFORMATION,
                    project,
                    Lists.newArrayList(new RecoverAction(), new IgnoreAction()));
        });

    }

}

