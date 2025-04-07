package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.node.JsonTreeNode;
import cn.memoryzy.json.util.JsonUtil;
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

public class CopyKeyValueAction extends DumbAwareAction implements UpdateInBackground {
    private final Tree tree;

    public CopyKeyValueAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.copy.kv.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.copy.kv.description"),
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
                JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                // 获取value值，多个的话用其他处理方式
                Object value = node.getValue();
                JsonTreeNodeType nodeType = node.getNodeType();

                String userObject = node.getUserObject().toString();
                // 只有JSONArrayElement是没有Value的
                if (Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeType)) {
                    valueList.add(userObject);
                } else if (Objects.equals(JsonTreeNodeType.JSONObjectProperty, nodeType)) {
                    valueList.add(userObject + ": " + (Objects.nonNull(value) ? value.toString() : "null"));
                } else {
                    JsonWrapper json = (JsonWrapper) value;

                    String item;
                    // 深度拷贝为另一个对象，并去除注释
                    if (Objects.nonNull(json)) {
                        json = json.cloneAndRemoveCommentKey();
                        item = JsonUtil.formatJson(json);
                    } else {
                        item = "null";
                    }

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