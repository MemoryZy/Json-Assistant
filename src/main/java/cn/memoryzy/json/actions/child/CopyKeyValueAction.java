package cn.memoryzy.json.actions.child;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.enums.JsonTreeNodeValueTypeEnum;
import cn.memoryzy.json.ui.treenode.JsonCollectInfoMutableTreeNode;
import cn.memoryzy.json.utils.JsonUtil;
import cn.memoryzy.json.utils.PlatformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CopyKeyValueAction extends AnAction {

    private final Tree tree;

    public CopyKeyValueAction(Tree tree) {
        super(JsonAssistantBundle.message("action.json.structure.window.copy.key.value.text"));
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
                JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();

                String userObject = node.getUserObject().toString();
                // 只有JSONArrayEl是没有Value的
                if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONArrayEl, nodeValueType)) {
                    valueList.add(userObject);
                } else if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONObjectKey, nodeValueType)) {
                    valueList.add(userObject + ": " + (Objects.nonNull(correspondingValue) ? correspondingValue.toString() : "null"));
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

                    valueList.add(userObject + ": " + item);
                }
            }

            PlatformUtil.setClipboard(StrUtil.join(", \n", valueList));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TreePath[] paths = tree.getSelectionPaths();
        boolean visible = true;
        if (Objects.nonNull(paths) && paths.length == 1) {
            TreePath path = paths[0];
            JsonCollectInfoMutableTreeNode node = (JsonCollectInfoMutableTreeNode) path.getLastPathComponent();
            JsonTreeNodeValueTypeEnum nodeValueType = node.getValueType();
            if (Objects.equals(JsonTreeNodeValueTypeEnum.JSONArrayEl, nodeValueType)) {
                visible = false;
            }
        }

        e.getPresentation().setEnabledAndVisible(visible);
    }
}