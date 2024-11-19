package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class JsonBeautifyToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final EditorEx editor;

    public JsonBeautifyToolWindowAction(@NotNull EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.beautify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.beautify.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.PENCIL_STAR);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt B"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        GlobalJsonConverter.parseAndProcessJson(
                event.getDataContext(), editor, true,
                JsonAssistantBundle.messageOnSystem("hint.selection.beautify"),
                JsonAssistantBundle.messageOnSystem("hint.global.beautify"));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor));
    }

}
