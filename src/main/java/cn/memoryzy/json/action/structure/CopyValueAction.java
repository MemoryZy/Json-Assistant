package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.ui.component.node.JsonTreeNode;
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

public class CopyValueAction extends DumbAwareAction {
    private final Tree tree;

    public CopyValueAction(Tree tree) {
        super(JsonAssistantBundle.message("action.structure.copy.value.text"),
                JsonAssistantBundle.messageOnSystem("action.structure.copy.value.description"),
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
                // JSONArrayElement及JSONObjectProperty都是普通类型
                if (Objects.equals(JsonTreeNodeType.JSONArrayElement, nodeType)
                        || Objects.equals(JsonTreeNodeType.JSONObjectProperty, nodeType)) {
                    valueList.add(Objects.nonNull(value) ? value.toString() : "null");
                } else {
                    JsonWrapper json = (JsonWrapper) value;
                    String item = Objects.nonNull(json) ? JsonUtil.formatJson(json) : "null";
                    valueList.add(item);
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", valueList));
        }
    }
}