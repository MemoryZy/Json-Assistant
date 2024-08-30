package cn.memoryzy.json.actions.child.toolwindow;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/21
 */
public class ClearEditorAction extends DumbAwareAction implements UpdateInBackground {

    private final JsonViewerWindow window;

    public ClearEditorAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.clear.editor.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.clear.editor.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.DELETE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        window.getJsonTextField().setText("");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(StrUtil.isNotBlank(window.getJsonContent()));
    }
}
