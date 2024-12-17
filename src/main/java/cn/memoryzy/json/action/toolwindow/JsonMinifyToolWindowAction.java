package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/8/24
 */
public class JsonMinifyToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final EditorEx editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public JsonMinifyToolWindowAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.minify.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.MINIFY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalJsonConverter.parseAndProcessJson(
                dataContext, editor, false,
                JsonAssistantBundle.messageOnSystem("hint.selection.minify"),
                JsonAssistantBundle.messageOnSystem("hint.global.minify"));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor)
                && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel));
    }
}
