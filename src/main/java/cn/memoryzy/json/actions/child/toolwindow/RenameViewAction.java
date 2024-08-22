package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.PluginConstant;
import cn.memoryzy.json.utils.UIManager;
import com.intellij.openapi.actionSystem.*;
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
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class RenameViewAction extends DumbAwareAction implements UpdateInBackground {

    public RenameViewAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.rename.view.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.rename.view.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Component contextComponent = e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
        BaseLabel tabLabel = contextComponent instanceof BaseLabel ? (BaseLabel) contextComponent: e.getData(ToolWindowContentUi.SELECTED_CONTENT_TAB_LABEL);
        if (tabLabel == null) return;
        Content content = tabLabel.getContent();
        showContentRenamePopup(tabLabel, Objects.requireNonNull(content));
    }

    private void showContentRenamePopup(BaseLabel baseLabel, Content content) {
        JBTextField textField = new JBTextField(content.getDisplayName());
        textField.selectAll();

        JBLabel label = new JBLabel(JsonAssistantBundle.message("balloon.rename.view.label.text"));
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
            public void keyPressed(KeyEvent e) {
                if (e != null && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //noinspection deprecation
                    if (!Disposer.isDisposed(content)) {
                        if (StrUtil.isBlank(textField.getText())) {
                            // 将输入框边框红色，以示警告
                            UIManager.addErrorBorder(textField);
                            return;
                        }

                        content.setDisplayName(textField.getText());
                    }
                    balloon.hide();
                }
            }
        });

        UIManager.addRemoveErrorListener(textField);
        balloon.show(new RelativePoint(baseLabel, new Point(baseLabel.getWidth() / 2, baseLabel.getHeight())), Balloon.Position.above);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ToolWindow toolWindow = e.getDataContext().getData(PlatformDataKeys.TOOL_WINDOW);
        if (toolWindow == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Content content = getContextContent(e);
        e.getPresentation().setEnabledAndVisible(project != null
                && Objects.equals(PluginConstant.JSON_VIEWER_TOOLWINDOW_ID, toolWindow.getId())
                && content != null);
    }

    @Nullable
    private static Content getContextContent(@NotNull AnActionEvent e, @NotNull ToolWindow toolWindow) {
        Content selectedContent = getContextContent(e);
        if (selectedContent == null) {
            selectedContent = toolWindow.getContentManager().getSelectedContent();
        }
        return selectedContent;
    }

    private static Content getContextContent(@NotNull AnActionEvent e) {
        BaseLabel baseLabel = ObjectUtils.tryCast(e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT), BaseLabel.class);
        return baseLabel != null ? baseLabel.getContent() : null;
    }
}
