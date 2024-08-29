package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.JsonPathPanel;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.ui.listener.HyperLinkListenerImpl;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathFilterOnTextFieldAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {
    public static final String JSON_PATH_GUIDE_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathGuide";

    private final JsonViewerWindow window;

    public JsonPathFilterOnTextFieldAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SEARCH);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {

        // todo 再来个 learn more
        String s = "JSONPath 是一种用于查询 JSON 文档的语言<br/>数据提取、数据验证、数据转换、数据操作";

        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                if (Registry.is("ide.helptooltip.enabled")) {
                    HelpTooltip.dispose(this);
                    new HelpTooltip()
                            .setTitle(getTemplatePresentation().getText())
                            .setDescription(s)
                            .installOn(this);
                } else {
                    setToolTipText(IdeBundle.message("search.everywhere.action.tooltip.text", "", "classesTabName"));
                }
            }
        };

        // SearchEverywhereAction

        button.setBorder(JBUI.Borders.empty());

        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        showComponentPopup(e, project);
    }

    private void showComponentPopup(@NotNull AnActionEvent e, Project project) {
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
        popup.show(calculatePopupLocation(e));

        // 弹出指引
        showGuidePopup(popup, expressionComboBoxTextField);
    }


    private RelativePoint calculatePopupLocation(@NotNull AnActionEvent e) {
        Component source = (Component) e.getInputEvent().getSource();
        Container toolbar = source.getParent();
        Container toolWindowPanel = toolbar.getParent();
        Component[] components = toolbar.getComponents();
        Component firstAction = components[0];
        return new RelativePoint(source, new Point((toolWindowPanel.getWidth() / 2 - 35), firstAction.getY() - firstAction.getHeight()));
    }


    private void showGuidePopup(JBPopup popup, JComponent component) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean hasShown = propertiesComponent.getBoolean(JSON_PATH_GUIDE_KEY, false);
        if (!hasShown) {
            String message = JsonAssistantBundle.messageOnSystem("balloon.json.path.guide.popup.content", HyperLinks.JSONPATH_EXPRESS_DESCRIPTION);

            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(message, null, JBColor.white, new HyperLinkListenerImpl())
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
        e.getPresentation().setEnabled(JsonAssistantUtil.isJsonOrExtract(window.getJsonContent()));
    }

}
