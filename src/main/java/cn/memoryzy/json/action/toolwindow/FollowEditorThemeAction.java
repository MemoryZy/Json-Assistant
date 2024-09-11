package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.ui.component.JsonViewerPanel;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.DelegateColorScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class FollowEditorThemeAction extends ToggleAction implements DumbAware {
    private static final Logger LOG = Logger.getInstance(DisplayLineNumberAction.class);
    public static final String FOLLOW_EDITOR_THEME_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".FollowEditorTheme";

    private final ToolWindowEx toolWindow;

    public FollowEditorThemeAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.follow.editor.theme.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.follow.editor.theme.description"));

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(FOLLOW_EDITOR_THEME_ENABLED_KEY);
        if (value == null) propertiesComponent.setValue(FOLLOW_EDITOR_THEME_ENABLED_KEY, Boolean.TRUE.toString());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return isFollowEditorTheme();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
        JsonViewerPanel viewerPanel = JsonAssistantUtil.getPanelOnContent(content);
        if (viewerPanel == null) {
            LOG.error("[Follow Editor Theme] Editor is null");
            propertiesComponent.setValue(FOLLOW_EDITOR_THEME_ENABLED_KEY, Boolean.FALSE.toString());
            return;
        }

        EditorEx editor = viewerPanel.getEditor();
        EditorColorsScheme defaultColorsScheme = viewerPanel.getDefaultColorsScheme();
        ApplicationManager.getApplication().invokeLater(() -> changeColorSchema(editor, defaultColorsScheme, state));

        if (state) propertiesComponent.setValue(FOLLOW_EDITOR_THEME_ENABLED_KEY, Boolean.TRUE.toString());
        else propertiesComponent.setValue(FOLLOW_EDITOR_THEME_ENABLED_KEY, Boolean.FALSE.toString());
    }

    public static boolean isFollowEditorTheme() {
        return Boolean.TRUE.toString().equals(PropertiesComponent.getInstance().getValue(FOLLOW_EDITOR_THEME_ENABLED_KEY));
    }

    public static void changeColorSchema(EditorEx editor, EditorColorsScheme defaultColorsScheme, boolean followEditorColor) {
        // true：跟随IDE配色；false：改为新配色
        if (followEditorColor) {
            editor.setColorsScheme(defaultColorsScheme);
        } else {
            DelegateColorScheme scheme = ConsoleViewUtil.updateConsoleColorScheme(defaultColorsScheme);
            if (UISettings.getInstance().getPresentationMode()) {
                scheme.setEditorFontSize(UISettings.getInstance().getPresentationModeFontSize());
            }
            editor.setColorsScheme(scheme);
        }
    }

}
