package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.HistoryEntry;
import cn.memoryzy.json.model.HistoryLimitedList;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonHistoryPersistentState;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.content.BaseLabel;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class RenameTabAction extends DumbAwareAction implements UpdateInBackground {

    public RenameTabAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.rename.tab.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.rename.tab.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Component contextComponent = event.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        BaseLabel tabLabel = contextComponent instanceof BaseLabel ? (BaseLabel) contextComponent : event.getData(ToolWindowContentUi.SELECTED_CONTENT_TAB_LABEL);
        if (tabLabel == null) return;
        Content content = tabLabel.getContent();
        showContentRenamePopup(getEventProject(event), tabLabel, Objects.requireNonNull(content));
    }

    private void showContentRenamePopup(Project project, BaseLabel baseLabel, Content content) {
        JBTextField textField = new JBTextField(content.getDisplayName());
        textField.selectAll();

        JBLabel label = new JBLabel(JsonAssistantBundle.message("popup.rename.view.label.text"));
        label.setFont(StartupUiUtil.getLabelFont().deriveFont(Font.BOLD));

        JPanel panel = SwingHelper.newLeftAlignedVerticalPanel(label, Box.createVerticalStrut(JBUI.scale(2)), textField);
        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                IdeFocusManager.findInstance().requestFocus(textField, false);
            }
        });

        Balloon balloon = JBPopupFactory.getInstance().createDialogBalloonBuilder(panel, null)
                .setShowCallout(true)
                .setCloseButtonEnabled(false)
                .setAnimationCycle(0)
                .setDisposable(content)
                .setHideOnKeyOutside(true)
                .setHideOnClickOutside(true)
                .setRequestFocus(true)
                .setBlockClicksThroughBalloon(true)
                .createBalloon();

        textField.addKeyListener(new KeyAdapter() {
            @Override
            @SuppressWarnings("deprecation")
            public void keyPressed(KeyEvent e) {
                if (e != null && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!Disposer.isDisposed(content)) {
                        if (StrUtil.isBlank(textField.getText())) {
                            // 将输入框边框红色，以示警告
                            UIManager.addErrorBorder(textField);
                            return;
                        }

                        String name = textField.getText();
                        content.setDisplayName(name);

                        asyncUpdateName(project, name, content);
                    }
                    balloon.hide();
                }
            }
        });

        UIManager.addRemoveErrorListener(textField);
        balloon.show(new RelativePoint(baseLabel, new Point(baseLabel.getWidth() / 2, baseLabel.getHeight())), Balloon.Position.above);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean enabled = false;
        Project project = event.getProject();
        ToolWindow toolWindow = event.getData(PlatformDataKeys.TOOL_WINDOW);
        if (toolWindow != null) {
            String id = toolWindow.getId();
            Content content = ToolWindowUtil.getContextContent(event.getDataContext());
            enabled = project != null
                    && (Objects.equals(PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID, id) || Objects.equals(PluginConstant.AUXILIARY_TREE_TOOLWINDOW_ID, id))
                    && content != null;
        }

        event.getPresentation().setEnabledAndVisible(enabled);
    }

    private void asyncUpdateName(Project project, String name, Content content) {
        // 寻找历史记录，找到相同记录，更改名称
        ApplicationManager.getApplication().invokeLater(() -> {
            if (isDefaultName(name)) {
                return;
            }

            EditorEx editor = ToolWindowUtil.getEditorOnContent(content);
            if (editor == null) {
                return;
            }

            String text = editor.getDocument().getText();
            if (StrUtil.isBlank(text)) {
                return;
            }

            JsonWrapper jsonWrapper = null;
            if (JsonUtil.isJson(text)) {
                jsonWrapper = JsonUtil.parse(text);
            } else if (Json5Util.isJson5(text)) {
                jsonWrapper = Json5Util.parse(text);
            }

            if (jsonWrapper == null) {
                return;
            }

            HistoryLimitedList history = JsonHistoryPersistentState.getInstance(project).getHistory();
            for (HistoryEntry entry : history) {
                if (Objects.equals(jsonWrapper, entry.getJsonWrapper())) {
                    entry.setName(name);
                }
            }
        });
    }

    private boolean isDefaultName(String name) {
        if (StrUtil.equalsIgnoreCase(PluginConstant.JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME, name)) {
            return true;
        }

        if (name.length() >= 4) {
            String prefix = name.substring(0, 4);
            String postfix = name.substring(4);
            return StrUtil.equalsIgnoreCase(PluginConstant.JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME, prefix) && ReUtil.isMatch("\\d+", postfix);
        }

        return true;
    }
}
