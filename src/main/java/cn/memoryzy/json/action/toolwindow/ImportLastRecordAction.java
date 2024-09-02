package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/2
 */
public class ImportLastRecordAction extends ToggleAction implements CustomComponentAction, DumbAware {

    public static final String IMPORT_RECORD_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + "Import.Last.Record";

    public ImportLastRecordAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.IMPORT_HISTORY);

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(IMPORT_RECORD_ENABLED_KEY);
        if (value == null) propertiesComponent.setValue(IMPORT_RECORD_ENABLED_KEY, Boolean.TRUE.toString());
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
                            .setDescription(JsonAssistantBundle.messageOnSystem("help.tooltip.paste.history.action.description"))
                            .installOn(this);
                } else {
                    setToolTipText(JsonAssistantBundle.messageOnSystem("help.tooltip.paste.history.action.description"));
                }
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return Boolean.TRUE.toString().equals(PropertiesComponent.getInstance().getValue(IMPORT_RECORD_ENABLED_KEY));
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PropertiesComponent.getInstance().setValue(IMPORT_RECORD_ENABLED_KEY, Boolean.valueOf(state).toString());
    }
}
