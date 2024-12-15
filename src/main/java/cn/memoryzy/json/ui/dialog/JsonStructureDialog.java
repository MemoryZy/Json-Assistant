package cn.memoryzy.json.ui.dialog;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.constant.FileTypeHolder;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.enums.TreeDisplayMode;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.GeneralState;
import cn.memoryzy.json.toolwindow.AuxiliaryTreeToolWindowManager;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import cn.memoryzy.json.ui.component.JsonAssistantToolWindowPanel;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.ToolWindowUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Memory
 * @since 2024/3/7
 */
public class JsonStructureDialog extends DialogWrapper {

    private Tree tree;
    private final JsonWrapper wrapper;

    public JsonStructureDialog(JsonWrapper wrapper) {
        super((Project) null, true);
        this.wrapper = wrapper;

        setModal(false);
        setTitle(JsonAssistantBundle.messageOnSystem("dialog.structure.title"));
        setOKButtonText(JsonAssistantBundle.messageOnSystem("dialog.ok.button"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JsonStructureComponentProvider componentProvider = new JsonStructureComponentProvider(wrapper, getRootPane(), true);
        tree = componentProvider.getTree();
        JPanel rootPanel = componentProvider.getTreeComponent();
        rootPanel.setPreferredSize(new Dimension(400, 470));
        return rootPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(getOKAction());
        actions.add(getHelpAction());
        return actions.toArray(new Action[0]);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return tree;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return UrlType.SITE_TREE.getId();
    }

    @Override
    public void show() {
        ApplicationManager.getApplication().invokeLater(super::show);
    }


    public static void show(DataContext dataContext, String text, boolean isJson, StructureActionSource source) {
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        JsonWrapper jsonWrapper = isJson ? JsonUtil.parse(JsonUtil.ensureJson(text)) : Json5Util.parse(text);
        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
        GeneralState generalState = persistentState.generalState;

        if (generalState.treeDisplayMode == TreeDisplayMode.POPUP) {
            // 弹窗展示
            new JsonStructureDialog(jsonWrapper).show();

        } else if (generalState.treeDisplayMode == TreeDisplayMode.ORIGINAL_TOOLWINDOW) {
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
        Optional.ofNullable(panelOnContent).ifPresent(panel -> panel.switchToCard(jsonWrapper, false));

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
