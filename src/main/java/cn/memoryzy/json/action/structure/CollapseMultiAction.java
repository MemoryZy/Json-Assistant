package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.util.UIManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

public class CollapseMultiAction extends DumbAwareAction implements UpdateInBackground {
    private final Tree tree;

    public CollapseMultiAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.collapse.multi.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.collapse.multi.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                UIManager.collapseAll(tree, path);
            }
        }
    }


    @Override
    @SuppressWarnings("DuplicatedCode")
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(ExpandMultiAction.isEnabled(tree));
    }
}
