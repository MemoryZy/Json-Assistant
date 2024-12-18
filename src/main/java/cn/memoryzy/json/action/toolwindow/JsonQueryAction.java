package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.util.ReflectUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.JsonPathComponentProvider;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.ex.EditorEx;
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
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonQueryAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    private final EditorEx editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public static final String JSON_PATH_GUIDE_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathGuide";

    public JsonQueryAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.jsonpath.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.jsonpath.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.SEARCH);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt P"), simpleToolWindowPanel);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        ActionButton button = new ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
            @Override
            protected void updateToolTipText() {
                HelpTooltip.dispose(this);
                // noinspection DialogTitleCapitalization
                HelpTooltip helpTooltip = new HelpTooltip()
                        .setTitle(getTemplatePresentation().getText())
                        .setShortcut(getShortcut())
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.json.path.description"));

                setExternalLink(helpTooltip);
                helpTooltip.installOn(this);
            }

            private void setExternalLink(HelpTooltip helpTooltip) {
                String name = JsonAssistantBundle.messageOnSystem("tooltip.json.path.link");
                Runnable action = () -> BrowserUtil.browse(Urls.JSONPATH_EXPRESS_DESCRIPTION);
                boolean external = true;

                // 带有 boolean external 的方法
                Object[] hasExternalLinkMethodParams = {name, action, external};
                Method method = JsonAssistantUtil.getMethod(helpTooltip, "setLink", hasExternalLinkMethodParams);

                if (Objects.nonNull(method)) {
                    ReflectUtil.invoke(helpTooltip, method, hasExternalLinkMethodParams);
                } else {
                    Object[] noExternalLinkMethodParams = {name, action};
                    method = JsonAssistantUtil.getMethod(helpTooltip, "setLink", noExternalLinkMethodParams);
                    if (Objects.nonNull(method)) ReflectUtil.invoke(helpTooltip, method, noExternalLinkMethodParams);
                }
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        // showComponentPopup(project);

        Optional.ofNullable(simpleToolWindowPanel.getContent())
                .ifPresent(el -> ((JsonAssistantToolWindowPanel) el)
                        .switchToCard(null, PluginConstant.JSON_QUERY_CARD_NAME));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(
                GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor)
                        && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel));
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+P";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
    }


    private void showComponentPopup(Project project) {
        JsonPathComponentProvider provider = new JsonPathComponentProvider(project, editor);
        JPanel rootPanel = provider.createComponent();
        JComponent expressionComboBoxTextField = provider.getPathExpressionComboBoxTextField();

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean hasShown = propertiesComponent.getBoolean(JSON_PATH_GUIDE_KEY, false);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rootPanel, hasShown ? expressionComboBoxTextField : null)
                .setFocusable(true)
                .setTitle(JsonAssistantBundle.messageOnSystem("popup.jsonpath.title"))
                .setCancelButton(new IconButton(JsonAssistantBundle.messageOnSystem("tooltip.json.path.cancel"), AllIcons.General.HideToolWindow))
                .setShowShadow(true)
                .setShowBorder(true)
                .setLocateWithinScreenBounds(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(false)
                .setCancelKeyEnabled(true)
                .setMovable(true)
                .setResizable(true)
                .setLocateByContent(true)
                .setAdText("你好", SwingConstants.LEFT)
                .setNormalWindowLevel(true)
                .createPopup();

        // Enter
        DumbAwareAction.create(event -> provider.getAction().run())
                .registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), expressionComboBoxTextField, popup);

        // Alt+向上箭头
        DumbAwareAction.create(event -> provider.searchHistory(true))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("alt UP"), expressionComboBoxTextField, popup);

        // Alt+向下箭头
        DumbAwareAction.create(event -> provider.searchHistory(false))
                .registerCustomShortcutSet(CustomShortcutSet.fromString("alt DOWN"), expressionComboBoxTextField, popup);

        // 弹出
        popup.show(calculatePopupLocation());

        // 弹出指引
        showGuidePopup(popup, expressionComboBoxTextField);
    }

    private RelativePoint calculatePopupLocation() {
        JComponent toolbar = simpleToolWindowPanel.getToolbar();
        Component[] components = Objects.requireNonNull(toolbar).getComponents();
        Component firstAction = components[0];
        return new RelativePoint(toolbar, new Point((simpleToolWindowPanel.getWidth() / 2 - 90), firstAction.getY()));
    }

    private void showGuidePopup(JBPopup popup, JComponent component) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean hasShown = propertiesComponent.getBoolean(JSON_PATH_GUIDE_KEY, false);
        if (!hasShown) {
            String message = JsonAssistantBundle.messageOnSystem("popup.jsonpath.guide.content");
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

}
