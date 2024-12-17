package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.action.JsonStructureAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
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
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public JsonStructureToolWindowAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.structure.toolwindow.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.STRUCTURE);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt T"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String text = editor.getDocument().getText();
        JsonStructureAction.show(event.getDataContext(), text, JsonUtil.canResolveToJson(text), StructureActionSource.TOOLWINDOW_TOOLBAR);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(event), editor)
                && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel));
    }

}