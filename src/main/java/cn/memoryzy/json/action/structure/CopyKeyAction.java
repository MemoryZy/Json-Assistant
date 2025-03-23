package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CopyKeyAction extends DumbAwareAction implements UpdateInBackground {
    private final Tree tree;

    public CopyKeyAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.copy.key.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.copy.key.description"),
                null);
        this.tree = tree;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            List<String> keyList = new ArrayList<>();
            for (TreePath path : paths) {
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                // 有key
                if (!JsonTreeNodeType.JSONArrayElement.equals(node.getNodeType())) {
                    // key
                    keyList.add(node.getUserObject().toString());
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", ", keyList));
        }
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(isVisible(tree));
    }

    public static boolean isVisible(Tree tree) {
        TreePath[] paths = tree.getSelectionPaths();
        if (Objects.nonNull(paths) && paths.length == 1) {
            TreePath path = paths[0];
            JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
            JsonTreeNodeType nodeType = node.getNodeType();
            return !Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeType);
        }

        return true;
    }

}