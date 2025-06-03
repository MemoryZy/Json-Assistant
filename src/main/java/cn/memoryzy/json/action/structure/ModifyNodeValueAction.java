package cn.memoryzy.json.action.structure;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/6/3
 */
public class ModifyNodeValueAction extends DumbAwareAction implements UpdateInBackground {

    private final Tree tree;

    public ModifyNodeValueAction(Tree tree) {
        super(JsonAssistantBundle.messageOnSystem("action.structure.modify.node.value.text"), JsonAssistantBundle.messageOnSystem("action.structure.modify.node.value.description"), null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
