package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeTypeEnum;
import cn.memoryzy.json.ui.node.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CopyKeyAction extends DumbAwareAction {

    private final Tree tree;

    public CopyKeyAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.copy.key.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.copy.key.description"),
                null);
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
                if (!JsonTreeNodeTypeEnum.JSONArrayEl.equals(node.getValueType())) {
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
            JsonTreeNodeTypeEnum nodeValueType = node.getValueType();
            if (Objects.equals(JsonTreeNodeTypeEnum.JSONArrayEl, nodeValueType)) {
                visible = false;
            }
        }

        e.getPresentation().setEnabledAndVisible(visible);
    }
}