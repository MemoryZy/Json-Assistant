package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.ui.JsonViewerWindow;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/2
 */
public class DisplayLineNumberAction extends ToggleAction implements DumbAware {

    public static final String DISPLAY_LINE_NUMBER_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + "Display.Line.Number";

    private final JsonViewerWindow window;

    public DisplayLineNumberAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.display.line.number.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.display.line.number.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.NUMBER);

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(DISPLAY_LINE_NUMBER_ENABLED_KEY);
        if (value == null) propertiesComponent.setValue(DISPLAY_LINE_NUMBER_ENABLED_KEY, Boolean.TRUE.toString());
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {

    }
}
