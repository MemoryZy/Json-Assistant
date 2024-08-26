package cn.memoryzy.json.actions;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/26
 */
public class HideEditorToolbarAction extends DumbAwareAction {

    public HideEditorToolbarAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.hide.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.editor.toolbar.hide.description"));
        presentation.setIcon(AllIcons.Actions.Close);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
