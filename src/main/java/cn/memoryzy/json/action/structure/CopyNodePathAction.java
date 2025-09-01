package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.ui.tree.JsonFilterableTree;
import cn.memoryzy.json.ui.tree.JsonTreeNode2;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/1/20
 */
public class CopyNodePathAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;

    public CopyNodePathAction(Tree tree) {
        super(JsonAssistantBundle.messageOnSystem("action.structure.copy.node.path.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.copy.node.path.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath[] selectPaths = tree.getSelectionPaths();
        if (ArrayUtil.isNotEmpty(selectPaths)) {
            List<String> pathList = new ArrayList<>();
            for (TreePath path : selectPaths) {
                JsonTreeNode2 node = JsonFilterableTree.getNode(path);
                pathList.add(node.getJsonPath());
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", pathList));
        }
    }

}
