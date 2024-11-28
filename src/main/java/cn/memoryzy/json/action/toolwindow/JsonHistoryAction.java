package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.ui.dialog.JsonHistoryChooser;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.Notifications;
import cn.memoryzy.json.util.PlatformUtil;
import com.google.common.collect.Lists;
import com.intellij.conversion.ComponentManagerSettings;
import com.intellij.ide.HelpTooltip;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonHistoryAction extends DumbAwareAction implements CustomComponentAction {

    private static final Logger LOG = Logger.getInstance(JsonHistoryAction.class);

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
            // 项目数据（.idea/misc.xml）
            Path path = projectDataManagerSettings.getPath();
            // 根节点
            Element rootElement = projectDataManagerSettings.getRootElement();

            // 获取之前版本历史记录的 State Key
            Element historyElement = projectDataManagerSettings.getComponentElement("JsonAssistantJsonHistory");
            // 获取属性值
            String oriHistory = (historyElement == null) ? null : historyElement.getAttributeValue("historyList");

            // 没有数据的话，退出
            if (StrUtil.isBlank(oriHistory) && !JsonUtil.isJsonArray(oriHistory)) {
                return;
            }

            ArrayWrapper array = JsonUtil.parseArray(oriHistory);
            if (array.isEmpty()) {
                return;
            }

            // 与当前版本存在的历史记录做匹配，看看是否有匹配项，有的话就不计入
            List<JsonWrapper> oldHistory = new ArrayList<>();
            HistoryLimitedList newHistory = JsonHistoryPersistentState.getInstance(project).getHistory();
            for (Object data : array) {
                JsonWrapper wrapper = null;
                String dataStr = (String) data;
                if (JsonUtil.isJson(dataStr)) {
                    if (JsonUtil.isJsonObject(dataStr)) {
                        wrapper = JsonUtil.parseObject(dataStr);
                    } else {
                        wrapper = JsonUtil.parseArray(dataStr);
                    }
                } else if (Json5Util.isJson5(dataStr)) {
                    if (Json5Util.isJson5Object(dataStr)) {
                        wrapper = Json5Util.parseObject(dataStr);
                    } else {
                        wrapper = Json5Util.parseArray(dataStr);
                    }
                }

                // 此Json是否存在
                if (Objects.isNull(wrapper) || newHistory.exists(wrapper)) {
                    continue;
                }

                oldHistory.add(wrapper);
            }

            // 都存在于现在的历史记录的话，就结束
            if (oldHistory.isEmpty()) {
                return;
            }

            NotificationAction importAction = NotificationAction.createExpiring(JsonAssistantBundle.messageOnSystem("action.recover.history.text"),
                    (event, notification) -> importRecords(project, oldHistory, newHistory, path, rootElement, historyElement));

            NotificationAction ignoreAction = NotificationAction.createExpiring(JsonAssistantBundle.messageOnSystem("action.ignore.text"),
                    (event, notification) -> ignoreRecords(path, rootElement, historyElement));

            ArrayList<NotificationAction> notificationActions = Lists.newArrayList(importAction, ignoreAction);

            Notifications.showFullStickyNotification(
                    "Json Assistant",
                    JsonAssistantBundle.messageOnSystem("notification.recover.content", oldHistory.size()),
                    NotificationType.INFORMATION,
                    project,
                    notificationActions);
        });
    }

    private static void importRecords(Project project, List<JsonWrapper> oldHistory, HistoryLimitedList newHistory,
                                      Path path, Element rootElement, Element historyElement) {
        for (JsonWrapper wrapper : oldHistory) {
            newHistory.add(project, wrapper);
        }

        removeOldRecord(path, rootElement, historyElement);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            LOG.error(e);
        }

        Notifications.showNotification(JsonAssistantBundle.messageOnSystem("notification.recover.success.content"), NotificationType.INFORMATION, project);
    }

    private static void ignoreRecords(Path path, Element rootElement, Element historyElement) {
        // 标记
        removeOldRecord(path, rootElement, historyElement);
    }

    /**
     * 移除旧版本记录
     */
    private static void removeOldRecord(Path path, Element rootElement, Element historyElement) {
        try {
            boolean removed = rootElement.removeContent(historyElement);
            if (removed) {
                JDOMUtil.write(rootElement, path);
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}

