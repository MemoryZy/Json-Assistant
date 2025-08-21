package cn.memoryzy.json.ui.tree;

import cn.hutool.core.date.DatePattern;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/8/20
 */
public class HistoryFilterableTree extends FilterableTree<DefaultMutableTreeNode, HistoryNode> {

    public HistoryFilterableTree(@NotNull Project project, @NotNull HistoryNode root) {
        super(project, new Tree(), new DefaultMutableTreeNode(root));
    }

    @Override
    protected Class<? extends DefaultMutableTreeNode> getNodeClass() {
        return DefaultMutableTreeNode.class;
    }

    @Override
    protected @NotNull DefaultMutableTreeNode createNode(@NotNull HistoryNode node) {
        return new DefaultMutableTreeNode(node);
    }

    @Override
    protected @NotNull Iterable<HistoryNode> getChildren(@NotNull HistoryNode node) {
        return HistoryTreeNodeType.GROUP == node.getNodeType() || HistoryTreeNodeType.ROOT == node.getNodeType()
                ? node.getChildren()
                : Collections.emptyList();
    }

    @Override
    protected @Nullable String getText(@Nullable HistoryNode node) {
        return null == node ? null : node.toString();
    }

    @Override
    protected void rebuildTree() {
        HistoryNode rootNode = getUserObject(getRoot());
        if (null == rootNode) return;
        rootNode.clear();

        // 根据更新时间分组记录
        HistoryManager historyManager = HistoryManager.getInstance(getProject());
        Map<String, List<JsonRecord>> group = historyManager.groupByUpdateTime();
        // 将时间进行排序
        List<Map.Entry<String, List<JsonRecord>>> recordList = group.entrySet().stream()
                .sorted(Comparator.comparing(
                        el -> PluginConstant.UNKNOWN.equals(el.getKey())
                                ? LocalDate.MIN
                                : LocalDate.parse(el.getKey(), DatePattern.NORM_DATE_FORMATTER)))
                .collect(Collectors.toList());

        // 反转
        Collections.reverse(recordList);

        for (Map.Entry<String, List<JsonRecord>> entry : recordList) {
            String key = entry.getKey();
            List<JsonRecord> value = entry.getValue();

            // 排序List
            value.sort(Comparator.comparing(JsonRecord::getUpdateTime).reversed());

            // Map第一层是组节点
            HistoryNode groupNode = new HistoryNode(null, key, value.size(), HistoryTreeNodeType.GROUP);

            // 添加底层数据节点
            for (JsonRecord record : value) {
                // Map第二层是具体数据节点
                groupNode.add(new HistoryNode(record, null, null, HistoryTreeNodeType.NODE));
            }

            rootNode.add(groupNode);
        }
    }

    @Override
    public void update() {
        super.update();
        getSearchModel().updateStructure();
    }

}
