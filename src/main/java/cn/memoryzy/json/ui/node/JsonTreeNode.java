package cn.memoryzy.json.ui.node;


import cn.memoryzy.json.enums.JsonTreeNodeType;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.Map;

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

    /**
     * 节点注释
     */
    private String comment;

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

    public String getComment() {
        return comment;
    }

    public JsonTreeNode setComment(String comment) {
        this.comment = comment;
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

    public boolean isObject() {
        return nodeType == JsonTreeNodeType.JSONObject || nodeType == JsonTreeNodeType.JSONObjectElement;
    }

    public boolean isArray() {
        return nodeType == JsonTreeNodeType.JSONArray || nodeType == JsonTreeNodeType.JSONArrayElementArray;
    }

    public boolean isPrimitive() {
        return nodeType == JsonTreeNodeType.JSONArrayElement || nodeType == JsonTreeNodeType.JSONObjectProperty;
    }

    // /**
    //  * 更新节点的JSON值（智能处理不同类型）
    //  *
    //  * @param newValue 新值
    //  * @return 是否成功更新
    //  */
    // public boolean updateJsonValue(Object newValue) {
    //     // 1. 基本类型直接更新父节点的对应值
    //     if (isPrimitive()) {
    //         return updateParentForPrimitive(newValue);
    //     }
    //
    //     // 2. 对象或数组类型
    //     // 类型不变时（如对象->对象，数组->数组），直接更新引用
    //     if ((isObject() && newValue instanceof Map) || (isArray() && newValue instanceof List)) {
    //         // 更新值绑定
    //         this.value = newValue;
    //         return true;
    //     }
    //
    //     // 3. 类型变更（如数组->对象），需要父节点执行更新
    //     return notifyParentForTypeChange(newValue);
    // }

    // /**
    //  * 为基本类型节点更新父节点中的值
    //  */
    // private boolean updateParentForPrimitive(Object newValue) {
    //     if (parent == null) {
    //         System.err.println("基本类型根节点不能直接更新");
    //         return false;
    //     }
    //
    //     if (parent.isObject()) {
    //         // 父节点是对象 (JSON Object)
    //         Map<String, Object> parentMap = (Map<String, Object>) parent.jsonValue;
    //         parentMap.put((String) keyInParent, newValue);
    //
    //         // 更新自己的值引用
    //         this.jsonValue = newValue;
    //         return true;
    //     } else if (parent.isArray()) {
    //         // 父节点是数组 (JSON Array)
    //         List<Object> parentList = (List<Object>) parent.jsonValue;
    //         int index = (Integer) keyInParent;
    //
    //         // 确保索引有效
    //         if (index >= 0 && index < parentList.size()) {
    //             parentList.set(index, newValue);
    //
    //             // 更新自己的值引用
    //             this.jsonValue = newValue;
    //             return true;
    //         }
    //         System.err.println("数组索引无效: " + index);
    //         return false;
    //     }
    //
    //     System.err.println("不支持更新父节点类型: " + parent.nodeType);
    //     return false;
    // }
    //
    // /**
    //  * 类型变更时需要父节点更新引用
    //  */
    // private boolean notifyParentForTypeChange(Object newValue) {
    //     JsonTreeNode parentNode = (JsonTreeNode) parent;
    //     if (parentNode == null) {
    //         // 根节点类型变更
    //         this.value = newValue;
    //         setNodeType(resolveNodeType(newValue));
    //         return true;
    //     }
    //
    //     if (parentNode.isObject()) {
    //         // 父节点是对象
    //         Map<String, Object> parentMap = (Map<String, Object>) parentNode.value;
    //         parentMap.put((String) keyInParent, newValue);
    //
    //         // 更新当前节点
    //         this.jsonValue = newValue;
    //         setNodeType();
    //         return true;
    //
    //     } else if (parent.isArray()) {
    //         // 父节点是数组
    //         List<Object> parentList = (List<Object>) parent.jsonValue;
    //         int index = (Integer) keyInParent;
    //
    //         if (index >= 0 && index < parentList.size()) {
    //             parentList.set(index, newValue);
    //
    //             // 更新当前节点
    //             this.jsonValue = newValue;
    //             setNodeType();
    //             return true;
    //         }
    //
    //         System.err.println("数组索引无效: " + index);
    //         return false;
    //     }
    //
    //     return false;
    // }

    private JsonTreeNodeType resolveNodeType(Object newValue) {
        JsonTreeNode parentNode = (JsonTreeNode) parent;
        JsonTreeNodeType parentNodeType = parentNode.nodeType;
        boolean isArrayParent = parentNodeType == JsonTreeNodeType.JSONArray
                || parentNodeType == JsonTreeNodeType.JSONArrayElementArray;

        if (newValue instanceof Map) {
            // 判断父节点是否为数组类型
            return isArrayParent ? JsonTreeNodeType.JSONObjectElement : JsonTreeNodeType.JSONObject;
        }

        if (newValue instanceof List) {
            return isArrayParent ? JsonTreeNodeType.JSONArrayElementArray : JsonTreeNodeType.JSONArray;
        }

        if (isArrayParent) {
            return JsonTreeNodeType.JSONArrayElement;
        }

        // if (parentNodeType == JsonTreeNodeType.JSONObject) {
        return JsonTreeNodeType.JSONObjectProperty;
        // }
    }

}
