package cn.memoryzy.json.action;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.ui.dialog.JsonStructureDialog;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ToolWindow;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAction extends DumbAwareAction {

    public JsonStructureAction() {
        super();
        setEnabledInModalContext(true);
        Presentation presentation = getTemplatePresentation();
        presentation.setText(JsonAssistantBundle.message("action.structure.text"));
        presentation.setDescription(JsonAssistantBundle.messageOnSystem("action.structure.description"));
        presentation.setIcon(JsonAssistantIcons.STRUCTURE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        ToolWindow toolWindow = PlatformDataKeys.TOOL_WINDOW.getData(dataContext);
        StructureActionSource source =
                Objects.nonNull(toolWindow) && PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID.equals(toolWindow.getId())
                        ? StructureActionSource.TOOLWINDOW_EDITOR
                        : StructureActionSource.OUTSIDE;

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        JsonStructureDialog.show(event.getDataContext(), json, GlobalJsonConverter.isValidJson(context.getProcessor()), source);
    }

}
