package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
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

/**
 * @author Memory
 * @since 2025/4/15
 */
public class CopyNodeCommentAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;

    public CopyNodeCommentAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.copy.node.comment.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.copy.node.comment.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            List<String> commentList = new ArrayList<>();

            for (TreePath path : paths) {
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                String comment = node.getComment();
                if (StrUtil.isNotBlank(comment)) {
                    commentList.add(comment);
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", ", commentList));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;

        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                String comment = node.getComment();
                if (StrUtil.isNotBlank(comment)) {
                    enabled = true;
                    break;
                }
            }
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
