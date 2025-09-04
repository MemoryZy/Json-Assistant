package cn.memoryzy.json.action;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.enums.TreeViewMode;
import cn.memoryzy.json.model.EditorContext;
import cn.memoryzy.json.model.strategy.GlobalJsonConverter;
import cn.memoryzy.json.model.strategy.GlobalTextConverter;
import cn.memoryzy.json.model.strategy.formats.context.GlobalTextConversionProcessorContext;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.toolwindow.AuxiliaryTreeToolWindowManager;
import cn.memoryzy.json.ui.JsonQueryComponentProvider;
import cn.memoryzy.json.ui.dialog.JsonStructureDialog;
import cn.memoryzy.json.ui.panel.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.*;
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
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = PlatformUtil.getEditor(dataContext);

        // 如果是标记的编辑器，那么就用弹窗
        boolean queryEditorFlag = Boolean.TRUE.equals(editor.getUserData(JsonQueryComponentProvider.QUERY_EDITOR_FLAG));
        ToolWindow toolWindow = PlatformDataKeys.TOOL_WINDOW.getData(dataContext);
        StructureActionSource source =
                Objects.nonNull(toolWindow) && PluginConstant.JSON_ASSISTANT_TOOLWINDOW_ID.equals(toolWindow.getId())
                        ? StructureActionSource.TOOLWINDOW_EDITOR
                        : StructureActionSource.OUTSIDE;

        GlobalTextConversionProcessorContext context = new GlobalTextConversionProcessorContext();
        EditorData editorData = GlobalTextConverter.resolveEditor(editor);
        if (null == editorData) return;

        editorData.setParseComment(true);
        String json = GlobalJsonConverter.parseJson(context, editorData);
        show(dataContext, editor, json, source, queryEditorFlag);
    }


    /**
     * 按设置展示树
     *
     * @param dataContext     数据上下文
     * @param text            JSON文本
     * @param source          事件触发来源
     * @param queryEditorFlag 是否为查询界面的编辑器
     */
    public static void show(DataContext dataContext,
                            Editor editor,
                            String text,
                            StructureActionSource source,
                            boolean queryEditorFlag) {

        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        // 获取编辑器及文件上下文
        EditorContext editorContext = PlatformUtil.getEditorContext(project, editor);
        // 解析 JSON
        JsonWrapper jsonWrapper = JsonUtil.isJson(text) ? JsonUtil.parse(JsonUtil.ensureJson(text)) : Json5Util.parseWithComment(text);

        TreeViewMode treeDisplayMode;
        if (queryEditorFlag) {
            treeDisplayMode = TreeViewMode.POPUP;
        } else {
            treeDisplayMode = GeneralSettings.getInstance().getState().getTreeStructureState().getTreeViewMode();
        }

        if (treeDisplayMode == TreeViewMode.POPUP) {
            // 弹窗展示
            new JsonStructureDialog(jsonWrapper, editorContext).show();

        } else if (treeDisplayMode == TreeViewMode.ORIGINAL_TOOLWINDOW) {
            // 在旧窗口展示
            showInOriginalToolWindow(project, editorContext, jsonWrapper, source, UIUtils.JSON_TREE_CARD_NAME);

        } else {
            // 在新辅助窗口展示
            showInAuxiliaryToolWindow(project, jsonWrapper, editorContext);
        }
    }

    public static void showInOriginalToolWindow(Project project,
                                                EditorContext editorContext,
                                                JsonWrapper jsonWrapper,
                                                StructureActionSource source,
                                                String cardName) {

        // 原本的工具窗口窗口（Json Assistant）展示
        ToolWindowEx toolWindow = (ToolWindowEx) ToolWindowUtil.getJsonAssistantToolWindow(project);

        // 如果是 Toolbar Action 或 ToolWindow 的编辑器内打开的，那么就获取当前选中的标签页
        Content content = ToolWindowUtil.getSelectedContent(toolWindow);
        JsonAssistantToolWindowPanel panelOnContent = ToolWindowUtil.getPanelOnContent(content);

        if (StructureActionSource.OUTSIDE.equals(source)) {
            // 如果是其他地方的，那么判断当前标签页是否存在文本，存在则用此标签页，不存在则新开标签页
            boolean hasText = Optional.ofNullable(panelOnContent)
                    .map(JsonAssistantToolWindowPanel::getEditor)
                    .map(EditorEx::getDocument)
                    .map(document -> StrUtil.isNotBlank(document.getText()))
                    .orElse(false);

            // 有文本，新开标签页
            if (hasText) {
                content = ToolWindowUtil.addNewContent(project, toolWindow, ContentFactory.SERVICE.getInstance(), FileTypeHolder.JSON5, null);
                panelOnContent = ToolWindowUtil.getPanelOnContent(content);
            }
        }

        // 打开窗口
        toolWindow.show();

        // 获取标签页的面板，切换卡片
        Optional.ofNullable(panelOnContent).ifPresent(panel -> panel.switchToCard(jsonWrapper, editorContext, cardName));
    }

    public static void showInAuxiliaryToolWindow(Project project, JsonWrapper jsonWrapper, EditorContext editorContext) {
        // 新开工具窗口展示
        AuxiliaryTreeToolWindowManager manager = AuxiliaryTreeToolWindowManager.getInstance(project);
        // 转换并展示
        manager.convertAndShow(jsonWrapper, editorContext);
    }

}
