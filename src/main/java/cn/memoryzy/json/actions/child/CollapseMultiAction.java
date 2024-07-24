package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.ArrayUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeValueTypeEnum;
import cn.memoryzy.json.ui.treenode.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.utils.TreeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;

public class CollapseMultiAction extends DumbAwareAction {
    private final Tree tree;

    public CollapseMultiAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.collapse.multi.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.collapse.multi.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                TreeUtil.collapseAll(tree, path);
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
                JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
                if (Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObject)
                        || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONArray)
                        || Objects.equals(nodeValueType, JsonTreeNodeValueTypeEnum.JSONObjectEl))
                    enabled = true;
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
