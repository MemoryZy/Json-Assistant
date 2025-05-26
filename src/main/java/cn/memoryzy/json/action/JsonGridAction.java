package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2025/5/26
 */
public class JsonGridAction extends DumbAwareAction implements UpdateInBackground {

    public JsonGridAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.grid.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.grid.description"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = PlatformUtil.getEditor(dataContext);

        ToolWindow toolWindow = PlatformDataKeys.TOOL_WINDOW.getData(dataContext);
        StructureActionSource source = Objects.nonNull(toolWindow) && PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID.equals(toolWindow.getId())
                ? StructureActionSource.TOOLWINDOW_EDITOR
                : StructureActionSource.OUTSIDE;

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (null == editorData) return;

        String json = GlobalJsonConverter.parseJson(context, editorData);
        boolean isJson = GlobalJsonConverter.isValidJson(context.getProcessor());
        JsonWrapper jsonWrapper = isJson ? JsonUtil.parse(JsonUtil.ensureJson(json)) : Json5Util.parseWithComment(json);
        JsonStructureAction.showInOriginalToolWindow(getEventProject(e), jsonWrapper, source, UIManager.JSON_GRID_CARD_NAME);
    }
}
