package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.ArrayUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;

public class ExpandMultiAction extends DumbAwareAction {

    private final Tree tree;

    public ExpandMultiAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.expand.multi.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.expand.multi.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                UIManager.expandAll(tree, path);
            }
        }
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(isEnabled(tree));
    }


    public static boolean isEnabled(Tree tree) {
        TreePath[] paths = tree.getSelectionPaths();
        if (ArrayUtil.isNotEmpty(paths)) {
            for (TreePath path : paths) {
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                JsonTreeNodeType nodeType = node.getNodeType();
                if (Objects.equals(nodeType, JsonTreeNodeType.JSONObject)
                        || Objects.equals(nodeType, JsonTreeNodeType.JSONArray)
                        || Objects.equals(nodeType, JsonTreeNodeType.JSONObjectElement))
                    return true;
            }
        }

        return false;
    }
}