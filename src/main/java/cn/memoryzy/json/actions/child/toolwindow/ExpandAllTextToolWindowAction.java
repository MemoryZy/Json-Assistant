package cn.memoryzy.json.actions.child.toolwindow;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.ui.JsonViewerWindow;
import cn.memoryzy.json.utils.JsonAssistantUtil;
import cn.memoryzy.json.utils.JsonUtil;
import com.intellij.codeInsight.folding.impl.actions.ExpandRegionAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.LanguageTextField;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class ExpandAllTextToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final JsonViewerWindow window;

    public ExpandAllTextToolWindowAction(JsonViewerWindow window) {
        super();
        this.window = window;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.expand.all.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.expand.all.description"));
        presentation.setIcon(JsonAssistantIcons.InnerAction.EXPAND_ALL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LanguageTextField jsonTextField = window.getJsonTextField();
        Editor editor = jsonTextField.getEditor();
        int jsonOutsetOffset = JsonUtil.findJsonOutsetCharacterOffset(jsonTextField.getText());

        ExpandRegionAction.expandRegionAtOffset(
                Objects.requireNonNull(e.getProject()),
                Objects.requireNonNull(editor),
                jsonOutsetOffset);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(null != e.getProject()
                && null != window.getJsonTextField().getEditor()
                && JsonAssistantUtil.isJsonOrExtract(window.getJsonContent()));
    }
}
