package cn.memoryzy.json.ui.tree;

import cn.memoryzy.json.enums.JsonTreeNodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/8/27
 */
public class JsonTreeNode2 {

    /**
     * 节点名称（用于过滤节点）
     */
    private Object key;

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

    /**
     * JSON路径（如：$.data.items[0].name）
     */
    private String jsonPath;

    /**
     * 子节点
     */
    private final List<JsonTreeNode2> children = new ArrayList<>();


    public JsonTreeNode2(Object key) {
        this.key = key;
    }

    public boolean isParentNode() {
        return JsonTreeNodeType.isParentNode(nodeType);
    }

    public boolean isLeafNode() {
        return JsonTreeNodeType.isLeafNode(nodeType);
    }

    public void add(JsonTreeNode2 node) {
        children.add(node);
    }

    public void clear() {
        children.clear();
    }


    @Override
    public String toString() {
        if (JsonTreeNodeType.JSONObject.equals(nodeType)
                || JsonTreeNodeType.JSONArray.equals(nodeType)
                || JsonTreeNodeType.JSONObjectElement.equals(nodeType)
                || JsonTreeNodeType.JSONArrayElement.equals(nodeType)) {
            // 对象、数组、数组下对象、数组下基本类型，直接匹配key名称
            return key.toString();
        } else {
            // key-value
            return key + ":" + value;
        }
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public JsonTreeNode2 setValue(Object value) {
        this.value = value;
        return this;
    }

    public JsonTreeNodeType getNodeType() {
        return nodeType;
    }

    public JsonTreeNode2 setNodeType(JsonTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public JsonTreeNode2 setSize(Integer size) {
        this.size = size;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public JsonTreeNode2 setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public JsonTreeNode2 setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

    public List<JsonTreeNode2> getChildren() {
        return children;
    }
}
