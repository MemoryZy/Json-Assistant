package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/8/9
 */
public class JsonQueryAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    private final EditorEx editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public JsonQueryAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.query.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.query.description"));
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
                        .setDescription(JsonAssistantBundle.messageOnSystem("tooltip.json.query.description", Urls.JSONPATH_EXPRESS_DESCRIPTION, Urls.JMESPATH_EXPRESS_DESCRIPTION));

                helpTooltip.installOn(this);
            }
        };

        button.setBorder(JBUI.Borders.empty(1, 2));
        return button;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Optional.ofNullable(simpleToolWindowPanel.getContent())
                .ifPresent(el -> ((JsonAssistantToolWindowPanel) el)
                        .switchToCard(null, PluginConstant.JSON_QUERY_CARD_NAME));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(
                GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor)
                        && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel)
                        && !editor.isViewer());
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();
        if (shortcuts.length == 0) {
            return (SystemInfo.isMac ? MacKeymapUtil.OPTION : "Alt") + "+P";
        }
        return KeymapUtil.getShortcutsText(shortcuts);
    }

}
