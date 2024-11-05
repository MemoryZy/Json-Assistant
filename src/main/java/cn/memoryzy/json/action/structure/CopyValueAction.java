package cn.memoryzy.json.action.structure;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.node.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.PlatformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        super(JsonAssistantBundle.message("action.json.structure.window.copy.value.text"),
                JsonAssistantBundle.messageOnSystem("action.json.structure.window.copy.value.description"),
                null);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            List<String> valueList = new ArrayList<>();
            for (TreePath path : paths) {
                JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
                // 获取value值，多个的话用其他处理方式
                Object correspondingValue = node.getCorrespondingValue();
                JsonTreeNodeType nodeValueType = node.getValueType();
                // 只有JSONArrayEl是没有Value的
                if (Objects.equals(JsonTreeNodeType.JSONArrayEl, nodeValueType)
                        || Objects.equals(JsonTreeNodeType.JSONObjectKey, nodeValueType)) {
                    valueList.add(Objects.nonNull(correspondingValue) ? correspondingValue.toString() : "null");
                } else {
                    JSON json = (JSON) correspondingValue;
                    String item;
                    if (Objects.nonNull(json)) {
                        try {
                            item = JsonUtil.MAPPER.writeValueAsString(json);
                        } catch (JsonProcessingException ex) {
                            item = json.toJSONString(2);
                        }
                    } else {
                        item = "null";
                    }

                    valueList.add(item);
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", valueList));
        }
    }
}