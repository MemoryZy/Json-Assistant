package cn.memoryzy.json.ui.component.node;


import cn.memoryzy.json.enums.JsonTreeNodeType;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Memory
 * @since 2024/2/28
 */
public class JsonCollectInfoMutableTreeNode extends DefaultMutableTreeNode {

    private Object value;
    private JsonTreeNodeType valueType;
    private Integer size;

    public JsonCollectInfoMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public JsonCollectInfoMutableTreeNode(Object userObject, Object value, JsonTreeNodeType valueType, Integer size) {
        super(userObject);
        this.value = value;
        this.valueType = valueType;
        this.size = size;
    }

    public Object getValue() {
        return value;
    }

    public JsonCollectInfoMutableTreeNode setValue(Object value) {
        this.value = value;
        return this;
    }

    public JsonTreeNodeType getValueType() {
        return valueType;
    }

    public JsonCollectInfoMutableTreeNode setValueType(JsonTreeNodeType valueType) {
        this.valueType = valueType;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public JsonCollectInfoMutableTreeNode setSize(Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        if (JsonTreeNodeType.JSONObject.equals(valueType)
                || JsonTreeNodeType.JSONArray.equals(valueType)
                || JsonTreeNodeType.JSONObjectElement.equals(valueType)
                || JsonTreeNodeType.JSONArrayElement.equals(valueType)) {
            // 对象、数组、数组下对象、数组下基本类型，直接匹配key名称
            return getUserObject().toString();
        } else {
            // key-value
            return getUserObject().toString() + ":" + value;
        }
    }
}
