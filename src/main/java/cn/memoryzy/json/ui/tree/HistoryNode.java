package cn.memoryzy.json.ui.tree;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.enums.HistoryTreeNodeType;
import cn.memoryzy.json.service.persistent.state.JsonRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2025/8/20
 */
public class HistoryNode {

    /**
     * 节点值（只有{@link HistoryTreeNodeType#NODE}类型才有值）
     */
    private JsonRecord value;

    /**
     * 组时间（只有{@link HistoryTreeNodeType#GROUP}类型才有值）
     */
    private String groupTime;

    /**
     * 组大小（只有{@link HistoryTreeNodeType#GROUP}类型才有值）
     */
    private Integer size;

    /**
     * 节点类型
     */
    private HistoryTreeNodeType nodeType;

    /**
     * 子节点
     */
    private final List<HistoryNode> children = new ArrayList<>();


    public HistoryNode() {
    }

    public HistoryNode(JsonRecord value, String groupTime, Integer size, HistoryTreeNodeType nodeType) {
        this.value = value;
        this.groupTime = groupTime;
        this.size = size;
        this.nodeType = nodeType;
    }

    public void add(HistoryNode node) {
        children.add(node);
    }

    public void clear() {
        children.clear();
    }

    public boolean isRoot() {
        return HistoryTreeNodeType.ROOT == nodeType;
    }

    public boolean isGroup() {
        return HistoryTreeNodeType.GROUP == nodeType;
    }

    public boolean isNode() {
        return HistoryTreeNodeType.NODE == nodeType;
    }


    public JsonRecord getValue() {
        return value;
    }

    public HistoryNode setValue(JsonRecord value) {
        this.value = value;
        return this;
    }

    public String getGroupTime() {
        return groupTime;
    }

    public HistoryNode setGroupTime(String groupTime) {
        this.groupTime = groupTime;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public HistoryNode setSize(Integer size) {
        this.size = size;
        return this;
    }

    public HistoryTreeNodeType getNodeType() {
        return nodeType;
    }

    public HistoryNode setNodeType(HistoryTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public List<HistoryNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        if (Objects.nonNull(nodeType) && HistoryTreeNodeType.ROOT != nodeType) {
            if (HistoryTreeNodeType.GROUP == nodeType) {
                return groupTime;
            } else {
                String name = value.getName();
                return StrUtil.isNotBlank(name) ? name : value.getDisplayText();
            }
        }

        return "";
    }

}
