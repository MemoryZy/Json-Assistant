package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonPathDialog;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathFilterOnTextFieldAction extends DumbAwareAction implements UpdateInBackground {
    private final JsonViewerWindow window;

    public JsonPathFilterOnTextFieldAction(JsonViewerWindow window) {
        super(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"),
                JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"),
                JsonAssistantIcons.SEARCH);
        this.window = window;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 4), source.getHeight() + 1));

        JsonPathDialog jsonPathDialog = new JsonPathDialog(project, window.getJsonTextField());
        JPanel rootPanel = jsonPathDialog.getRootPanel();
        JComponent jsonPathTextField = jsonPathDialog.getJsonPathTextField();

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, jsonPathTextField)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.on.text.field.title"))
                .setCancelButton(new IconButton(JsonAssistantBundle.messageOnSystem("popup.json.path.filter.cancel.btn.tooltip"), AllIcons.General.HideToolWindow))
                .setShowShadow(true)
                .setShowBorder(true)
                .setModalContext(false)
                .setLocateWithinScreenBounds(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(false)
                .setCancelKeyEnabled(true)
                .setMovable(true)
                .createPopup();

        // 注册回车动作
        Runnable action = (Runnable) rootPanel.getClientProperty(JsonPathDialog.TEXT_FIELD_PROPERTY_NAME);
        new DumbAwareAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                action.run();
            }
        }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke((char) KeyEvent.VK_ENTER)), jsonPathTextField, popup);

        // 弹出
        popup.show(relativePoint);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(StrUtil.isNotBlank(jsonStr));
    }

}
