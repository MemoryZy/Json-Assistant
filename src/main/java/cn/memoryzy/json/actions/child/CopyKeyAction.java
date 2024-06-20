package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeValueTypeEnum;
import cn.memoryzy.json.ui.treenode.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.utils.PlatformUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CopyKeyAction extends AnAction {

    private final Tree tree;

    public CopyKeyAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.copy.key.text"));
        this.tree = tree;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            List<String> keyList = new ArrayList<>();
            for (TreePath path : paths) {
                JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
                // 有key
                if (!JsonTreeNodeValueTypeEnum.JSONArrayEl.equals(node.getValueType())) {
                    // key
                    keyList.add(node.getUserObject().toString());
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", ", keyList));
        }
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        TreePath[] paths = tree.getSelectionPaths();
        boolean visible = true;
        if (Objects.nonNull(paths) && paths.length == 1) {
            TreePath path = paths[0];
            JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
            JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
            if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONArrayEl, nodeValueType)) {
                visible = false;
            }
        }

        e.getPresentation().setEnabledAndVisible(visible);
    }
}