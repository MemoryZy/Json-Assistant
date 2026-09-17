package cn.memoryzy.json.ui.tree;

import cn.hutool.core.collection.CollUtil;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.service.persistent.state.BaseData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/8/20
 */
public class HistoryNode extends BaseNode {

    /**
     * 节点类型
     */
    private HistoryTreeNodeType nodeType;

    /**
     * 关联数据对象 (Group 或 Note)
     */
    private BaseData value;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点图标
     */
    private Icon nodeIcon;

    /**
     * 原始完整路径（如：工作记录项目A）
     */
    private String path;

    /**
     * 子节点
     */
    private final List<HistoryNode> children = new ArrayList<>();

    /**
     * 父节点
     */
    private HistoryNode parentNode;


    public HistoryNode(String nodeName) {
        this.nodeName = nodeName;
    }

    // ---------------------------- 辅助方法 ---------------------------- //

    public void add(HistoryNode node) {
        children.add(node);
    }

    public void clear() {
        children.clear();
    }

    /**
     * 对当前节点及其所有后代节点进行递归排序
     *
     * @param comparator 用于排序的比较器，定义了排序规则
     */
    public void sortChildren(Comparator<HistoryNode> comparator) {
        if (CollUtil.isNotEmpty(children)) {
            // 1. 对当前节点的直接子节点列表进行排序
            children.sort(comparator);

            // 2. 遍历每个子节点，递归调用排序方法
            for (HistoryNode child : children) {
                child.sortChildren(comparator);
            }
        }
    }

    public boolean isRoot() {
        return nodeType == HistoryTreeNodeType.ROOT;
    }

    public boolean isNotRoot() {
        return nodeType != HistoryTreeNodeType.ROOT;
    }

    public boolean isGroup() {
        return nodeType == HistoryTreeNodeType.GROUP;
    }

    public boolean isRecord() {
        return nodeType == HistoryTreeNodeType.RECORD;
    }

    public String getId() {
        return null == value ? null : value.getId();
    }

    // ---------------------------- GETTER/SETTER ---------------------------- //

    public HistoryTreeNodeType getNodeType() {
        return nodeType;
    }

    public HistoryNode setNodeType(HistoryTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public BaseData getValue() {
        return value;
    }

    public HistoryNode setValue(BaseData value) {
        this.value = value;
        return this;
    }

    public List<HistoryNode> getChildren() {
        return children;
    }

    public String getNodeName() {
        return nodeName;
    }

    public HistoryNode setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public Icon getNodeIcon() {
        return nodeIcon;
    }

    public HistoryNode setNodeIcon(Icon nodeIcon) {
        this.nodeIcon = nodeIcon;
        return this;
    }

    public String getPath() {
        return path;
    }

    public HistoryNode setPath(String path) {
        this.path = path;
        return this;
    }

    public HistoryNode getParentNode() {
        return parentNode;
    }

    public HistoryNode setParentNode(HistoryNode parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    @Override
    public String toString() {
        return nodeName;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HistoryNode historyNode = (HistoryNode) o;
        return nodeType == historyNode.nodeType && Objects.equals(value, historyNode.value) && Objects.equals(nodeName, historyNode.nodeName) && Objects.equals(nodeIcon, historyNode.nodeIcon) && Objects.equals(path, historyNode.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, value, nodeName, nodeIcon, path);
    }
}
