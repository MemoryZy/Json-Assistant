package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.enums.TreeDisplayMode;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.GeneralState;
import cn.memoryzy.json.toolwindow.AuxiliaryTreeToolWindowManager;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import cn.memoryzy.json.ui.dialog.JsonStructureDialog;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.JsonAssistantIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/6/20
 */
public class JsonStructureAction extends DumbAwareAction implements UpdateInBackground {

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
        Editor editor = PlatformUtil.getEditor(dataContext);

        // 如果是标记的编辑器，那么就用弹窗
        boolean editorFlag = Boolean.TRUE.equals(editor.getUserData(JsonQueryComponentProvider.EDITOR_FLAG));
        ToolWindow toolWindow = PlatformDataKeys.TOOL_WINDOW.getData(dataContext);
        StructureActionSource source =
                Objects.nonNull(toolWindow) && PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID.equals(toolWindow.getId())
                        ? StructureActionSource.TOOLWINDOW_EDITOR
                        : StructureActionSource.OUTSIDE;

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        String json = GlobalJsonConverter.parseJson(context, PlatformUtil.getEditor(dataContext));
        show(event.getDataContext(), json, GlobalJsonConverter.isValidJson(context.getProcessor()), source, editorFlag);
    }


    public static void show(DataContext dataContext, String text, boolean isJson, StructureActionSource source, boolean editorFlag) {
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        JsonWrapper jsonWrapper = isJson ? JsonUtil.parse(JsonUtil.ensureJson(text)) : Json5Util.parse(text);

        TreeDisplayMode treeDisplayMode;
        if (editorFlag) {
            treeDisplayMode = TreeDisplayMode.POPUP;
        } else {
            JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
            GeneralState generalState = persistentState.generalState;
            treeDisplayMode = generalState.treeDisplayMode;
        }

        if (treeDisplayMode == TreeDisplayMode.POPUP) {
            // 弹窗展示
            new JsonStructureDialog(jsonWrapper).show();

        } else if (treeDisplayMode == TreeDisplayMode.ORIGINAL_TOOLWINDOW) {
            // 在旧窗口展示
            showInOriginalToolWindow(project, jsonWrapper, source);

        } else {
            // 在新辅助窗口展示
            showInAuxiliaryToolWindow(project, jsonWrapper);
        }
    }

    public static void showInOriginalToolWindow(Project project, JsonWrapper jsonWrapper, StructureActionSource source) {
        // 原本的工具窗口窗口（Json Assistant）展示
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantToolWindow(project);

        // 如果是 Toolbar Action 或 ToolWindow 的编辑器内打开的，那么就获取当前选中的标签页
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        JsonAssistantToolWindowPanel panelOnContent = ToolWindowUtil.getPanelOnContent(content);

        if (StructureActionSource.OUTSIDE.equals(source)) {
            // 如果是其他地方的，那么判断当前标签页是否存在文本，存在则用此标签页，不存在则新开标签页
            Boolean hasText = Optional.ofNullable(panelOnContent)
                    .map(JsonAssistantToolWindowPanel::getEditor)
                    .map(EditorEx::getDocument)
                    .map(document -> StrUtil.isNotBlank(document.getText()))
                    .orElse(false);

            // 有文本，新开标签页
            if (hasText) {
                content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), FileTypeHolder.JSON5);
                panelOnContent = ToolWindowUtil.getPanelOnContent(content);
            }
        }

        // 获取标签页的面板，切换卡片
        Optional.ofNullable(panelOnContent).ifPresent(panel -> panel.switchToCard(jsonWrapper, PluginConstant.JSON_TREE_CARD_NAME));

        // 打开窗口
        toolWindow.show();
    }

    public static void showInAuxiliaryToolWindow(Project project, JsonWrapper jsonWrapper) {
        // 新开工具窗口展示
        AuxiliaryTreeToolWindowManager manager = AuxiliaryTreeToolWindowManager.getInstance(project);
        // 转换并展示
        manager.convertAndShow(jsonWrapper);
    }

}
