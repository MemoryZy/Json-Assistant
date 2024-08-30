package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.constants.HyperLinks;
import cn.memoryzy.json.constants.JsonAssistantPlugin;
import cn.memoryzy.json.ui.JsonPathPanel;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonPathAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {
    public static final String JSON_PATH_GUIDE_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathGuide";

    private final JsonViewerWindow window;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public JsonPathAction(JsonViewerWindow window, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.window = window;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.path.filter.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.path.filter.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SEARCH);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt P"), simpleToolWindowPanel);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                if (Registry.is("ide.helptooltip.enabled")) {
                    HelpTooltip.dispose(this);
                    // noinspection DialogTitleCapitalization
                    new HelpTooltip()
                            .setTitle(getTemplatePresentation().getText())
                            .setShortcut(getShortcut())
                            .setDescription(JsonAssistantBundle.messageOnSystem("help.tooltip.json.path.action.description"))
                            .setLink(JsonAssistantBundle.messageOnSystem("help.tooltip.json.path.action.link"), () -> BrowserUtil.browse(HyperLinks.JSONPATH_EXPRESS_DESCRIPTION), true)
                            .installOn(this);
                } else {
                    setToolTipText(JsonAssistantBundle.messageOnSystem("help.tooltip.json.path.action.description"));
                }
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        showComponentPopup(e, project);
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+P";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
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
        JComponent toolbar = simpleToolWindowPanel.getToolbar();
        Component[] components = Objects.requireNonNull(toolbar).getComponents();
        Component firstAction = components[0];
        return new RelativePoint(toolbar, new Point((simpleToolWindowPanel.getWidth() / 2 - 35), firstAction.getY()));
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
        e.getPresentation().setEnabled(JsonAssistantUtil.isJsonOrExtract(window.getJsonContent()));
    }

}
