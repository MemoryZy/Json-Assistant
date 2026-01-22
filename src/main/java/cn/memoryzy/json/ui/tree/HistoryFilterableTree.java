package cn.memoryzy.json.ui.tree;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.service.persistent.HistoryManager;
import cn.memoryzy.json.service.persistent.state.JsonGroup;
import cn.memoryzy.json.service.persistent.state.JsonRecord;
import cn.memoryzy.json.ui.HistoryToolWindowComponentProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/8/17
 */
public class HistoryFilterableTree extends FilterableTree<DefaultMutableTreeNode, HistoryNode> {

    private final HistoryToolWindowComponentProvider provider;

    public HistoryFilterableTree(@NotNull Project project, @NotNull HistoryNode rootNode, HistoryToolWindowComponentProvider provider) {
        super(project, new Tree(), new DefaultMutableTreeNode(rootNode));
        this.provider = provider;
    }

    @Override
    protected Class<? extends DefaultMutableTreeNode> getNodeClass() {
        return DefaultMutableTreeNode.class;
    }

    @Override
    protected @NotNull DefaultMutableTreeNode createNode(@NotNull HistoryNode historyNode) {
        return new DefaultMutableTreeNode(historyNode);
    }

    @Override
    protected @NotNull Iterable<HistoryNode> getChildren(@NotNull HistoryNode historyNode) {
        // 获取非叶子节点的子节点列表
        return !historyNode.isRecord() ? historyNode.getChildren() : Collections.emptyList();
    }

    @Override
    protected @Nullable String getText(@Nullable HistoryNode historyNode) {
        return null == historyNode ? null : historyNode.getNodeName();
    }

    @Override
    public @NotNull SearchTextField installSearchField() {
        SearchTextField field = super.installSearchField();
        field.setOpaque(false);
        field.setBorder(JBUI.Borders.empty());

        JBTextField editor = field.getTextEditor();
        editor.setOpaque(false);
        editor.setBorder(JBUI.Borders.empty());
        editor.getEmptyText().setText(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));
        editor.getAccessibleContext().setAccessibleName(JsonAssistantBundle.messageOnSystem("toolwindow.history.search.empty.text"));

        return field;
    }

    @Override
    protected void configureTree(Tree tree) {
        TreeUtil.installActions(tree);
        // tree.setToggleClickCount(1);
    }

    @Override
    protected void rebuildTree() {
        // 重建树结构 (重构节点)
        HistoryNode rootNode = getRootUserObject();
        if (null == rootNode) return;
        rootNode.clear();

        HistoryManager manager = HistoryManager.getInstance(project);
        manager.replenishSourceFile();

        List<JsonRecord> records = manager.getRecords();
        List<JsonGroup> groups = manager.getGroups();

        // 创建目录节点映射表
        Map<String, HistoryNode> dirNodeMap = new HashMap<>();
        for (JsonGroup group : groups) {
            dirNodeMap.put(group.getId(),
                    new HistoryNode(group.getName())
                            .setNodeType(HistoryTreeNodeType.GROUP)
                            .setValue(group)
                            .setNodeIcon(AllIcons.Actions.ModuleDirectory));
        }

        // 构建层级关系
        for (JsonGroup group : groups) {
            HistoryNode currentNode = dirNodeMap.get(group.getId());
            String parentId = group.getParentId();

            if (parentId == null) {
                // 无父目录 -> 直接添加到 `全部记录`
                rootNode.add(currentNode);
                // 设置根节点为父目录
                currentNode.setParentNode(rootNode)
                        // 设置一级目录的路径
                        .setPath(rootNode.getPath() + "/" + currentNode.getNodeName());
            } else {
                // 查找父节点
                HistoryNode parentNode = dirNodeMap.get(parentId);
                // 找不到父目录 -> 作为一级目录处理
                HistoryNode newParentNode = Objects.requireNonNullElse(parentNode, rootNode);
                newParentNode.add(currentNode);
                // 设置父组
                currentNode.setParentNode(newParentNode)
                        // 设置当前组的路径
                        .setPath(newParentNode.getPath() + "/" + currentNode.getNodeName());
            }
        }

        // 挂载记录到目录
        for (JsonRecord record : records) {
            for (JsonGroup group : groups) {
                if (group.getRecordIds().contains(record.getId())) {
                    HistoryNode parentNode = dirNodeMap.get(group.getId());
                    if (parentNode != null) {
                        // 若无名称，则取短文本
                        String recordName = record.getName();
                        String displayText = record.getDisplayText();

                        parentNode.add(new HistoryNode(StrUtil.isNotBlank(recordName) ? recordName : displayText)
                                .setNodeType(HistoryTreeNodeType.RECORD)
                                .setValue(record)
                                .setNodeIcon(AllIcons.FileTypes.Json)
                                .setParentNode(parentNode)
                                .setPath(parentNode.getPath() + "/" + record.getName()));
                    }
                }
            }
        }

        // 汇总所有在目录下的记录ID
        Set<String> attachedNoteIds = groups.stream()
                .flatMap(dir -> dir.getRecordIds().stream())
                .collect(Collectors.toSet());

        // 添加孤立记录（不属于任何目录）
        for (JsonRecord record : records) {
            if (!attachedNoteIds.contains(record.getId())) {
                // 若无名称，则取短文本
                String recordName = record.getName();
                String displayText = record.getDisplayText();

                rootNode.add(new HistoryNode(StrUtil.isNotBlank(recordName) ? recordName : displayText)
                        .setNodeType(HistoryTreeNodeType.RECORD)
                        .setValue(record)
                        .setNodeIcon(AllIcons.FileTypes.Json)
                        .setParentNode(rootNode)
                        .setPath(rootNode.getPath() + "/" + record.getName()));
            }
        }

        Comparator<HistoryNode> complexComparator = Comparator
                // 第一优先级：按类型排序 (Group 优先于 Note)
                .comparing((HistoryNode node) -> {
                    if (node.getValue() instanceof JsonGroup) {
                        return 1; // Group 类型返回较高的优先级标识
                    } else if (node.getValue() instanceof JsonRecord) {
                        return 2; // Note 类型返回较低的优先级标识
                    }
                    return 3; // 处理其他未知类型或null，赋予最低优先级
                })

                // 第二优先级：按标题字典序排序，并处理null值
                .thenComparing(node -> {
                    String title = node.getNodeName();
                    return title != null ? title : ""; // 将null标题视为空字符串，避免排序异常
                }, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)) // 忽略大小写的字典序，null值排最后

                // 第三优先级（可选）：按更新时间降序，作为最终的排序条件
                .thenComparing(node -> node.getValue().getUpdatedTime(), Comparator.nullsLast(Comparator.reverseOrder()));

        rootNode.sortChildren(complexComparator);
    }

    @Override
    public void update() {
        super.update();
        getSearchModel().updateStructure();
    }

    @Override
    protected void handleNoMatch() {
        provider.refreshEditorPanel(null);
        provider.clearCurrentState();
    }
}
