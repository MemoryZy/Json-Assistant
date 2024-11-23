package cn.memoryzy.json.ui.component.node;

import cn.memoryzy.json.enums.HistoryGroupTreeNodeType;
import cn.memoryzy.json.model.HistoryEntry;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;

/**
 * @author Memory
 * @since 2024/11/22
 */
public class HistoryGroupTreeNode extends DefaultMutableTreeNode {

    /**
     * 节点值（只有{@link HistoryGroupTreeNodeType#NODE}类型才有值）
     */
    private HistoryEntry value;

    /**
     * 组时间（只有{@link HistoryGroupTreeNodeType#GROUP}类型才有值）
     */
    private String groupTime;

    /**
     * 组大小（只有{@link HistoryGroupTreeNodeType#GROUP}类型才有值）
     */
    private Integer size;

    /**
     * 节点类型
     */
    private HistoryGroupTreeNodeType nodeType;


    public HistoryGroupTreeNode() {
    }

    public HistoryGroupTreeNode(HistoryEntry value, String groupTime, Integer size, HistoryGroupTreeNodeType nodeType) {
        this.value = value;
        this.groupTime = groupTime;
        this.size = size;
        this.nodeType = nodeType;
    }

    public HistoryEntry getValue() {
        return value;
    }

    public HistoryGroupTreeNode setValue(HistoryEntry value) {
        this.value = value;
        return this;
    }

    public String getGroupTime() {
        return groupTime;
    }

    public HistoryGroupTreeNode setGroupTime(String groupTime) {
        this.groupTime = groupTime;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public HistoryGroupTreeNode setSize(Integer size) {
        this.size = size;
        return this;
    }

    public HistoryGroupTreeNodeType getNodeType() {
        return nodeType;
    }

    public HistoryGroupTreeNode setNodeType(HistoryGroupTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    @Override
    public String toString() {
        if (Objects.nonNull(nodeType)) {
            if (HistoryGroupTreeNodeType.GROUP.equals(nodeType)) {
                return groupTime;
            } else {
                return value.getShortText();
            }
        }

        return null;
    }
}
