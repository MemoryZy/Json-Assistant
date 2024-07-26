package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.ArrayUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeTypeEnum;
import cn.memoryzy.json.ui.treenode.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.utils.TreeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;

public class ExpandMultiAction extends DumbAwareAction {

    private final Tree tree;

    public ExpandMultiAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.expand.multi.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.expand.multi.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                TreeUtil.expandAll(tree, path);
            }
        }
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        TreePath[] paths = tree.getSelectionPaths();
        if (ArrayUtil.isNotEmpty(paths)) {
            for (TreePath path : paths) {
                JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
                JsonTreeNodeTypeEnum nodeValueType = node.getValueType();
                if (Objects.equals(nodeValueType, JsonTreeNodeTypeEnum.JSONObject)
                        || Objects.equals(nodeValueType, JsonTreeNodeTypeEnum.JSONArray)
                        || Objects.equals(nodeValueType, JsonTreeNodeTypeEnum.JSONObjectEl))
                    enabled = true;
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}