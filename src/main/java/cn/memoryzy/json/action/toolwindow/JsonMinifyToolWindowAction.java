package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.util.PlatformUtil;
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

    public JsonMinifyToolWindowAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.minify.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.minify.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.MINIFY);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        GlobalJsonConverter.parseAndProcessJson(
                dataContext, PlatformUtil.getEditor(dataContext), false,
                JsonAssistantBundle.messageOnSystem("hint.selection.json.minify.text"),
                JsonAssistantBundle.messageOnSystem("hint.global.json.minify.text"));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(
                GlobalJsonConverter.validateEditorAllJson(
                        getEventProject(event), editor, event.getDataContext()));
    }
}
