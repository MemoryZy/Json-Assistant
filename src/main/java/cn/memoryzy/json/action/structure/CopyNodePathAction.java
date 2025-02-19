package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
public class CopyNodePathAction extends DumbAwareAction {

    private final Tree tree;

    public CopyNodePathAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.copy.node.path.text"),
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
                StringBuilder pathString = new StringBuilder();
                Object[] pathElements = path.getPath();

                for (Object pathElement : pathElements) {
                    JsonTreeNode node = (JsonTreeNode) pathElement;
                    JsonTreeNodeType nodeType = node.getNodeType();

                    if (JsonTreeNodeType.JSONArrayElement == nodeType) {
                        JsonStructureComponentProvider.appendArrayElementPath(node, pathString);
                    } else {
                        pathString.append(pathString.length() > 0 ? " > " : "").append(node.getUserObject());
                    }
                }

                pathList.add(pathString.toString());
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", pathList));
        }
    }

}
