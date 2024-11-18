package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.component.node.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CopyKeyValueAction extends DumbAwareAction {

    private final Tree tree;

    public CopyKeyValueAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.copy.key.value.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.copy.key.value.description"),
                null);
        this.tree = tree;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            List<String> valueList = new ArrayList<>();
            for (TreePath path : paths) {
                JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
                // 获取value值，多个的话用其他处理方式
                Object value = node.getValue();
                JsonTreeNodeType nodeValueType = node.getValueType();

                String userObject = node.getUserObject().toString();
                // 只有JSONArrayElement是没有Value的
                if (Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeValueType)) {
                    valueList.add(userObject);
                } else if (Objects.equals(JsonTreeNodeType.JSONObjectProperty, nodeValueType)) {
                    valueList.add(userObject + ": " + (Objects.nonNull(value) ? value.toString() : "null"));
                } else {
                    JsonWrapper json = (JsonWrapper) value;
                    String item = Objects.nonNull(json) ? JsonUtil.formatJson(json) : "null";
                    valueList.add(userObject + ": " + item);
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", valueList));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(CopyKeyAction.isVisible(tree));
    }
}