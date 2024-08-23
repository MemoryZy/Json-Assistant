package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.JsonPathPanel;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
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
    public static final String JSON_PATH_GUIDE_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathGuide";
    
    private final JsonViewerWindow window;

    public JsonPathFilterOnTextFieldAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"));
        presentation.setIcon(JsonAssistantIcons.SEARCH);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        showComponentPopup(e, project);
    }

    private void showComponentPopup(@NotNull AnActionEvent e, Project project) {
        Component source = (Component) e.getInputEvent().getSource();
        RelativePoint relativePoint = new RelativePoint(source, new Point(-(source.getWidth() * 4 + 15), source.getHeight() + 1));

        JsonPathPanel jsonPathPanel = new JsonPathPanel(project, window.getJsonTextField());
        JPanel rootPanel = jsonPathPanel.getRootPanel();
        JComponent expressionComboBoxTextField = jsonPathPanel.getPathExpressionComboBoxTextField();

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean hasShown = propertiesComponent.getBoolean(JSON_PATH_GUIDE_KEY, false);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, hasShown ? expressionComboBoxTextField : null)
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

        // 弹出指引
        showGuidePopup(popup, expressionComboBoxTextField);
    }

    private void showGuidePopup(JBPopup popup, JComponent component) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean hasShown = propertiesComponent.getBoolean(JSON_PATH_GUIDE_KEY, false);
        if (!hasShown) {
            String message = JsonAssistantBundle.messageOnSystem("balloon.json.path.guide.popup.content");
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(message, null, JBColor.white, null)
                    .setShadow(true)
                    .setDisposable(popup)
                    .setHideOnAction(false)
                    .setHideOnClickOutside(true)
                    .setHideOnFrameResize(false)
                    .setHideOnKeyOutside(true)
                    .setHideOnLinkClick(false)
                    .setHideOnCloseClick(true)
                    .createBalloon()
                    .show(new RelativePoint(component, new Point(component.getWidth() / 2, component.getHeight())), Balloon.Position.below);

            propertiesComponent.setValue(JSON_PATH_GUIDE_KEY, true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = StrUtil.trim(window.getJsonContent());
        String jsonStr = (JsonUtil.isJsonStr(text)) ? text : JsonUtil.extractJsonStr(text);
        e.getPresentation().setEnabled(StrUtil.isNotBlank(jsonStr));
    }

}
