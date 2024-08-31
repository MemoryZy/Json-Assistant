package cn.memoryzy.json.action.child.toolwindow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.extension.JsonViewerEditorFloatingProvider;
import cn.memoryzy.json.model.LimitedList;
import cn.memoryzy.json.service.JsonViewerHistoryState;
import cn.memoryzy.json.ui.component.editor.FoldingLanguageTextEditor;
import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.MacKeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBUI;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class PasteHistoryEditorToolbarAction extends DumbAwareAction implements CustomComponentAction, UpdateInBackground {

    private final JsonViewerEditorFloatingProvider provider;

    public PasteHistoryEditorToolbarAction() {
        super();
        this.provider = JsonViewerEditorFloatingProvider.getInstance();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.paste.history.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.FORM_HISTORY);
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
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = PlatformUtil.getEditor(e);
        if (project == null || editor == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            LimitedList<String> history = JsonViewerHistoryState.getInstance(project).getHistory();
            if (CollUtil.isNotEmpty(history)) {
                editor.getDocument().setText(history.get(0));
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(validateActionEnabled(e));
    }

    private String getShortcut() {
        Shortcut[] shortcuts = getShortcutSet().getShortcuts();

        if (shortcuts.length == 0) {
            boolean isMac = SystemInfo.isMac;
            return (isMac ? MacKeymapUtil.COMMAND : "Ctrl") + "+" + (isMac ? MacKeymapUtil.OPTION : "Alt") + "+H";
        }

        return KeymapUtil.getShortcutsText(shortcuts);
    }

    private boolean validateActionEnabled(AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }

        ToolWindow toolWindow = JsonAssistantUtil.getJsonViewToolWindow(project);
        Content selectedContent = JsonAssistantUtil.getSelectedContent(toolWindow);

        Editor editor = PlatformUtil.getEditor(e);
        if (editor == null || selectedContent == null) return false;

        LimitedList<String> history = JsonViewerHistoryState.getInstance(project).getHistory();
        if (CollUtil.isEmpty(history)) {
            hideToolbarComponent(selectedContent);
            return false;
        }

        String userData = editor.getUserData(FoldingLanguageTextEditor.PLUGIN_EDITOR_KEY);
        if (!Objects.equals(JsonAssistantPlugin.PLUGIN_ID_NAME, userData)) {
            hideToolbarComponent(selectedContent);
            return false;
        }

        if (provider.isPermanentlyHide(selectedContent)) {
            hideToolbarComponent(selectedContent);
            return false;
        }

        String text = editor.getDocument().getText();
        if (StrUtil.isNotBlank(text)) {
            hideToolbarComponent(selectedContent);
            return false;
        } else {
            showToolbarComponent(selectedContent);
            return true;
        }
    }

    private void hideToolbarComponent(Content selectedContent) {
        // UI组件必须在EDT运行
        ApplicationManager.getApplication().invokeLater(() -> provider.hideToolbarComponent(selectedContent));
    }

    private void showToolbarComponent(Content selectedContent) {
        ApplicationManager.getApplication().invokeLater(() -> provider.showToolbarComponent(selectedContent));
    }
}
