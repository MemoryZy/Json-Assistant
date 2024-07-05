package cn.memoryzy.json.ui.treenode;


import cn.memoryzy.json.enums.JsonTreeNodeValueTypeEnum;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Memory
 * @since 2024/2/28
 */
public class JsonCollectInfoMutableTreeNode extends DefaultMutableTreeNode {

    private Object correspondingValue;
    private JsonTreeNodeValueTypeEnum valueType;
    private Integer size;

    public JsonCollectInfoMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public JsonCollectInfoMutableTreeNode(Object userObject, Object correspondingValue, JsonTreeNodeValueTypeEnum valueType, Integer size) {
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

    public JsonTreeNodeValueTypeEnum getValueType() {
        return valueType;
    }

    public JsonCollectInfoMutableTreeNode setValueType(JsonTreeNodeValueTypeEnum valueType) {
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
        if (JsonTreeNodeValueTypeEnum.JSONObject.equals(valueType)
                || JsonTreeNodeValueTypeEnum.JSONArray.equals(valueType)
                || JsonTreeNodeValueTypeEnum.JSONObjectEl.equals(valueType)
                || JsonTreeNodeValueTypeEnum.JSONArrayEl.equals(valueType)) {
            // 对象、数组、数组下对象、数组下基本类型，直接匹配key名称
            return getUserObject().toString();
        } else {
            // key-value
            return getUserObject().toString() + ":" + correspondingValue;
        }
    }
}
