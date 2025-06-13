package cn.memoryzy.json.action.toolwindow;

import cn.memoryzy.json.action.JsonStructureAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.UIUtils;
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
 * @since 2025/5/26
 */
public class JsonGridToolWindowAction extends DumbAwareAction implements UpdateInBackground {

    private final EditorEx editor;
    private final SimpleToolWindowPanel simpleToolWindowPanel;

    public JsonGridToolWindowAction(EditorEx editor, SimpleToolWindowPanel simpleToolWindowPanel) {
        super();
        this.editor = editor;
        this.simpleToolWindowPanel = simpleToolWindowPanel;
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.messageOnSystem("action.grid.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.grid.description"));
        presentation.setIcon(JsonAssistantIcons.ToolWindow.GRID);
        registerCustomShortcutSet(CustomShortcutSet.fromString("alt G"), simpleToolWindowPanel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (null == editorData) return;

        String json = GlobalJsonConverter.parseJson(context, editorData);
        JsonWrapper jsonWrapper = JsonUtil.isJson(json) ? JsonUtil.parse(JsonUtil.ensureJson(json)) : Json5Util.parseWithComment(json);
        JsonStructureAction.showInOriginalToolWindow(getEventProject(e), null, jsonWrapper, StructureActionSource.TOOLWINDOW_TOOLBAR, UIUtils.JSON_GRID_CARD_NAME);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(GlobalJsonConverter.validateEditorAllJson(getEventProject(e), editor)
                && JsonAssistantToolWindowPanel.isEditorCardDisplayed(simpleToolWindowPanel)
                && !editor.isViewer());
    }
}
