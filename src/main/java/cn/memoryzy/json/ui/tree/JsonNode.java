package cn.memoryzy.json.ui.tree;

import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/8/27
 */
public class JsonNode extends BaseNode {

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
     * 是否需要展开节点
     */
    private boolean isExpanded;

    /**
     * 父节点
     */
    private JsonNode parent;

    /**
     * 子节点
     */
    private final List<JsonNode> children = new ArrayList<>();


    public JsonNode(Object key) {
        this.key = key;
    }

    public boolean isParentNode() {
        return JsonTreeNodeType.isParentNode(nodeType);
    }

    public boolean isLeafNode() {
        return JsonTreeNodeType.isLeafNode(nodeType);
    }

    public void add(JsonNode node) {
        children.add(node);
    }

    public void clear() {
        children.clear();
    }

    public int getIndex(JsonNode node) {
        return children.indexOf(node);
    }

    public int getChildCount() {
        return children.size();
    }

    public JsonNode getChildAt(int index) {
        return children.get(index);
    }

    public void removeAllChildren() {
        children.clear();
    }

    /**
     * 删除节点并更新父节点及其祖先节点的 size
     */
    public void removeAndUpdateSize(JsonNode child) {
        // 1. 获取被删除节点在父节点中的索引
        int childIndex = this.getIndex(child);

        // 2. 从父节点中移除子节点
        children.remove(child);

        // 3. 更新父节点的大小
        updateSize();

        // 4. 如果当前节点是数组类型，更新后续节点的路径索引
        updateArrayPathsAfterEdit(childIndex);
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
                JsonNode parent = getParent();
                if (parent != null) {
                    parent.updateSize();
                }
            }
        }
    }

    /**
     * 更新数组元素的路径索引
     *
     * @param startIndex 开始更新的索引位置
     */
    public void updateArrayPathsAfterEdit(int startIndex) {
        // 确保当前节点是数组类型
        if (!isArray()) {
            return;
        }

        // 遍历所有子节点，从指定的起始索引开始
        for (int i = startIndex; i < children.size(); i++) {
            JsonNode child = getChildAt(i);

            // 为子节点构建新的路径
            String newPath = JsonFilterableTree.buildArrayElementPath(getJsonPath(), i);
            child.setJsonPath(newPath);

            // 递归更新子节点的路径（如果子节点包含数组元素）
            updateChildPaths(child);
        }
    }

    /**
     * 递归更新子节点路径
     */
    private void updateChildPaths(JsonNode parentNode) {
        for (int i = 0; i < parentNode.getChildren().size(); i++) {
            JsonNode child = parentNode.getChildAt(i);

            // 更新当前节点的路径
            updateNodePath(parentNode, child, i);

            // 递归更新子节点的路径
            if (child.getChildCount() > 0) {
                updateChildPaths(child);
            }
        }
    }

    /**
     * 更新单个节点的路径
     */
    private void updateNodePath(JsonNode parent, JsonNode child, int indexInParent) {
        String parentPath = parent.getJsonPath();
        String newPath;

        // 父节点是数组，使用索引路径
        newPath = JsonFilterableTree.buildArrayElementPath(parentPath, indexInParent);
        // 更新节点路径
        child.setJsonPath(newPath);
    }

    public boolean isArray() {
        return nodeType == JsonTreeNodeType.JSONArray || nodeType == JsonTreeNodeType.JSONArrayElementArray;
    }

    public boolean isPrimitive() {
        return nodeType == JsonTreeNodeType.JSONArrayElement || nodeType == JsonTreeNodeType.JSONObjectProperty;
    }

    @Override
    public String toString() {
        String keyString = String.valueOf(key);
        return JsonTreeNodeType.isParentNode(nodeType) ? keyString : keyString + ": " + JsonStructureComponentProvider.formatNodeValue(value);
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

    public JsonNode setValue(Object value) {
        this.value = value;
        return this;
    }

    public JsonTreeNodeType getNodeType() {
        return nodeType;
    }

    public JsonNode setNodeType(JsonTreeNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public JsonNode setSize(Integer size) {
        this.size = size;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public JsonNode setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public JsonNode setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public JsonNode getParent() {
        return parent;
    }

    public JsonNode setParent(JsonNode parent) {
        this.parent = parent;
        return this;
    }

    public List<JsonNode> getChildren() {
        return children;
    }
}
