package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonPathPanel;
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
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 4 + 15), source.getHeight() + 1));

        JsonPathPanel jsonPathPanel = new JsonPathPanel(project, window.getJsonTextField());
        JPanel rootPanel = jsonPathPanel.getRootPanel();
        JComponent expressionComboBoxTextField = jsonPathPanel.getPathExpressionComboBoxTextField();

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, expressionComboBoxTextField)
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

        // Enter
        DumbAwareAction.create(event -> jsonPathPanel.getAction().run())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), expressionComboBoxTextField, popup);

        // Alt+向上箭头
        DumbAwareAction.create(event -> jsonPathPanel.searchHistory(true))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("alt UP"), expressionComboBoxTextField, popup);

        // Alt+向下箭头
        DumbAwareAction.create(event -> jsonPathPanel.searchHistory(false))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("alt DOWN"), expressionComboBoxTextField, popup);

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
