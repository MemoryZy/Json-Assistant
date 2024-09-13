package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/9/11
 */
public class ConfigureEditorOptionsAction extends DumbAwareAction {

    public ConfigureEditorOptionsAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.configure.editor.options.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.configure.editor.options.description"));
        presentation.setIcon(AllIcons.General.GearPlain);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), JsonAssistantBundle.message("plugin.editor.options.configurable.displayName"));
    }
}
