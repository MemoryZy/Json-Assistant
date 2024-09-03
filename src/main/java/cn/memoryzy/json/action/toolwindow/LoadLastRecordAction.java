package cn.memoryzy.json.action.toolwindow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.util.JsonAssistantUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/2
 */
public class LoadLastRecordAction extends ToggleAction implements CustomComponentAction, DumbAware {
    public static final String IMPORT_RECORD_ENABLED_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".ImportLastRecord";

    private final ToolWindowEx toolWindow;

    public LoadLastRecordAction(ToolWindowEx toolWindow) {
        super();
        this.toolWindow = toolWindow;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.editor.toolbar.paste.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.description"));

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
        if (state) {
            Project project = getEventProject(e);
            if (project == null) return;
            Content content = JsonAssistantUtil.getSelectedContent(toolWindow);
            LanguageTextField languageTextField = JsonAssistantUtil.getLanguageTextFieldOnContent(content);
            if (languageTextField != null) {
                if (StrUtil.isBlank(languageTextField.getText())) {
                    JsonViewerHistoryState historyState = JsonViewerHistoryState.getInstance(project);
                    LimitedList<String> history = historyState.getHistory();
                    if (CollUtil.isNotEmpty(history)) {
                        languageTextField.setText(history.get(0));
                    }
                }
            }
        }

        PropertiesComponent.getInstance().setValue(IMPORT_RECORD_ENABLED_KEY, Boolean.valueOf(state).toString());
    }
}
