package cn.memoryzy.json.ui.component.node;


import cn.memoryzy.json.enums.JsonTreeNodeType;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Memory
 * @since 2024/2/28
 */
public class JsonTreeNode extends DefaultMutableTreeNode {

    /**
     * 节点值（父节点就是Json，底层节点就是具体值）
     */
    private Object value;

    /**
     * 节点类型
     */
    private JsonTreeNodeType nodeType;

    /**
     * 节点大小
     */
    private Integer size;


    public JsonTreeNode(Object userObject) {
        super(userObject);
    }

    public JsonTreeNode(Object userObject, Object value, JsonTreeNodeType nodeType, Integer size) {
        super(userObject);
        this.value = value;
        this.nodeType = nodeType;
        this.size = size;
    }

    public Object getValue() {
        return value;
    }

    public JsonTreeNode setValue(Object value) {
        this.value = value;
        return this;
    }

    public JsonTreeNodeType getNodeType() {
        return nodeType;
    }

    public JsonTreeNode setNodeType(JsonTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public JsonTreeNode setSize(Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        if (JsonTreeNodeType.JSONObject.equals(nodeType)
                || JsonTreeNodeType.JSONArray.equals(nodeType)
                || JsonTreeNodeType.JSONObjectElement.equals(nodeType)
                || JsonTreeNodeType.JSONArrayElement.equals(nodeType)) {
            // 对象、数组、数组下对象、数组下基本类型，直接匹配key名称
            return getUserObject().toString();
        } else {
            // key-value
            return getUserObject().toString() + ":" + value;
        }
    }

    /**
     * 更新节点及其所有父节点的 size
     */
    public void updateSize() {
        if (JsonTreeNodeType.JSONObject.equals(nodeType)
                || JsonTreeNodeType.JSONArray.equals(nodeType)
                || JsonTreeNodeType.JSONObjectElement.equals(nodeType)) {
            int newSize = getChildCount();
            if (newSize != this.size) {
                this.size = newSize;
                JsonTreeNode parent = (JsonTreeNode) getParent();
                if (parent != null) {
                    parent.updateSize();
                }
            }
        }
    }

    /**
     * 删除节点并更新父节点及其祖先节点的 size
     */
    public void removeAndUpdateSize(JsonTreeNode child) {
        remove(child);
        updateSize();
    }

}
