package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.dialog.JsonStructureDialog;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

public class JsonStructureToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final EditorEx editor;

    public JsonStructureToolWindowAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.json.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.json.structure.on.tw.title.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = editor.getDocument().getText();
        JsonStructureDialog.show(text, JsonUtil.canResolveToJson(text));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(
                GlobalJsonConverter.validateEditorAllJson(
                        getEventProject(event), editor, event.getDataContext()));
    }

}