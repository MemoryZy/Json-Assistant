package cn.memoryzy.json.ui.node;


import cn.memoryzy.json.enums.JsonTreeNodeTypeEnum;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Memory
 * @since 2024/2/28
 */
public class JsonCollectInfoMutableTreeNode extends DefaultMutableTreeNode {

    private Object correspondingValue;
    private JsonTreeNodeTypeEnum valueType;
    private Integer size;

    public JsonCollectInfoMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public JsonCollectInfoMutableTreeNode(Object userObject, Object correspondingValue, JsonTreeNodeTypeEnum valueType, Integer size) {
        super(userObject);
        this.correspondingValue = correspondingValue;
        this.valueType = valueType;
        this.size = size;
    }

    public Object getCorrespondingValue() {
        return correspondingValue;
    }

    public JsonCollectInfoMutableTreeNode setCorrespondingValue(Object correspondingValue) {
        this.correspondingValue = correspondingValue;
        return this;
    }

    public JsonTreeNodeTypeEnum getValueType() {
        return valueType;
    }

    public JsonCollectInfoMutableTreeNode setValueType(JsonTreeNodeTypeEnum valueType) {
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
        if (JsonTreeNodeTypeEnum.JSONObject.equals(valueType)
                || JsonTreeNodeTypeEnum.JSONArray.equals(valueType)
                || JsonTreeNodeTypeEnum.JSONObjectEl.equals(valueType)
                || JsonTreeNodeTypeEnum.JSONArrayEl.equals(valueType)) {
            // 对象、数组、数组下对象、数组下基本类型，直接匹配key名称
            return getUserObject().toString();
        } else {
            // key-value
            return getUserObject().toString() + ":" + correspondingValue;
        }
    }
}
