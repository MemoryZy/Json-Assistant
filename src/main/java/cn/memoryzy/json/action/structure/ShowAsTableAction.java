package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.ArrayUtil;
import cn.memoryzy.json.action.JsonStructureAction;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.enums.StructureActionSource;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.tree.JsonTreeNode;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/6/6
 */
public class ShowAsTableAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;

    public ShowAsTableAction(Tree tree) {
        super(JsonAssistantBundle.messageOnSystem("action.showAsTable.structure.text"), JsonAssistantBundle.messageOnSystem("action.showAsTable.structure.description"), null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath[] paths = tree.getSelectionPaths();
        JsonTreeNode node = (JsonTreeNode) Objects.requireNonNull(paths)[0].getLastPathComponent();
        JsonWrapper value = (JsonWrapper) node.getValue();
        JsonStructureAction.showInOriginalToolWindow(e.getProject(), null, value, StructureActionSource.OUTSIDE, UIUtils.JSON_GRID_CARD_NAME);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        TreePath[] paths = tree.getSelectionPaths();
        // 必须为单选
        if (Objects.nonNull(e.getProject()) && ArrayUtil.isNotEmpty(paths) && paths.length == 1) {
            // 必须为对象、数组类型节点
            JsonTreeNode node = (JsonTreeNode) paths[0].getLastPathComponent();
            JsonTreeNodeType nodeType = node.getNodeType();
            if (JsonTreeNodeType.isParentNode(nodeType)) {
                // 且必须存在子节点
                enabled = node.getChildCount() > 0;
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
