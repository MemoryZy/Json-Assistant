package cn.memoryzy.json.ui.dialog;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.TreeDisplayMode;
import cn.memoryzy.json.enums.UrlType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import cn.memoryzy.json.service.persistent.state.GeneralState;
import cn.memoryzy.json.toolwindow.AuxiliaryTreeToolWindowManager;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import cn.memoryzy.json.util.Json5Util;
import cn.memoryzy.json.util.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        JsonStructureComponentProvider componentProvider = new JsonStructureComponentProvider(wrapper, getRootPane());
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

    public static void show(Project project, String text, boolean isJson) {
        JsonWrapper jsonWrapper = isJson ? JsonUtil.parse(JsonUtil.ensureJson(text)) : Json5Util.parse(text);
        JsonAssistantPersistentState persistentState = JsonAssistantPersistentState.getInstance();
        GeneralState generalState = persistentState.generalState;

        if (generalState.treeDisplayMode == TreeDisplayMode.POPUP) {
            // 弹窗展示
            new JsonStructureDialog(jsonWrapper).show();
        } else if (generalState.treeDisplayMode == TreeDisplayMode.ORIGINAL_TOOLWINDOW) {
            // 原本的工具窗口窗口（Json Assistant）展示

            // TODO 当页面切换至树，那么toolbar的action都置为 不可用 状态

        } else {
            // 新开工具窗口展示
            AuxiliaryTreeToolWindowManager manager = AuxiliaryTreeToolWindowManager.getInstance(project);
            // 转换并展示
            manager.convertAndShow(jsonWrapper);
        }
    }

}
