package cn.memoryzy.json.ui.tree;


import cn.memoryzy.json.enums.JsonTreeNodeType;
import cn.memoryzy.json.ui.JsonStructureComponentProvider;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
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

    /**
     * JSON路径（如：$.data.items[0].name）
     */
    private String jsonPath;


    public JsonTreeNode(Object userObject) {
        super(userObject);
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

    public String getJsonPath() {
        return jsonPath;
    }

    public JsonTreeNode setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
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
        for (int i = startIndex; i < getChildCount(); i++) {
            JsonTreeNode child = (JsonTreeNode) getChildAt(i);

            // 为子节点构建新的路径
            String newPath = JsonStructureComponentProvider.buildArrayElementPath(getJsonPath(), i);
            child.setJsonPath(newPath);

            // 递归更新子节点的路径（如果子节点包含数组元素）
            updateChildPaths(child);
        }
    }

    /**
     * 递归更新子节点路径
     */
    private void updateChildPaths(JsonTreeNode parentNode) {
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            JsonTreeNode child = (JsonTreeNode) parentNode.getChildAt(i);

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
    private void updateNodePath(JsonTreeNode parent, JsonTreeNode child, int indexInParent) {
        String parentPath = parent.getJsonPath();
        String newPath;

        // 父节点是数组，使用索引路径
        newPath = JsonStructureComponentProvider.buildArrayElementPath(parentPath, indexInParent);
        // 更新节点路径
        child.setJsonPath(newPath);
    }

    /**
     * 删除节点并更新父节点及其祖先节点的 size
     */
    public void removeAndUpdateSize(JsonTreeNode child) {
        // 1. 获取被删除节点在父节点中的索引
        int childIndex = this.getIndex(child);

        // 2. 从父节点中移除子节点
        remove(child);

        // 3. 更新父节点的大小
        updateSize();

        // 4. 如果当前节点是数组类型，更新后续节点的路径索引
        updateArrayPathsAfterEdit(childIndex);
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


    /**
     * 获取路径的所有父路径
     */
    public List<String> getParentPaths() {
        List<String> paths = new ArrayList<>();
        if (jsonPath == null) return paths;

        // 从当前路径开始，逐级向上获取父路径
        String currentPath = jsonPath;
        while (currentPath != null && !currentPath.isEmpty()) {
            paths.add(0, currentPath); // 添加到开头以保证顺序
            currentPath = getParentPath(currentPath);
        }

        return paths;
    }

    /**
     * 从路径中获取父路径
     */
    private String getParentPath(String path) {
        // 处理数组路径: $.arr[3] -> $.arr
        if (path.endsWith("]")) {
            int startBracket = path.lastIndexOf('[');
            if (startBracket > 0) {
                return path.substring(0, startBracket);
            }
        }

        // 处理对象路径: $.obj.key -> $.obj
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }

        // 顶级路径: $.key -> $
        if (path.startsWith("$.")) {
            return "$";
        }

        return null;
    }

    /**
     * 获取相对于父节点的路径部分
     */
    public String getRelativePath() {
        if (jsonPath == null) return null;

        // 获取父路径
        String parentPath = getParent() != null ?
                ((JsonTreeNode) getParent()).getJsonPath() : null;

        // 如果没有父路径，返回完整路径
        if (parentPath == null || parentPath.isEmpty()) {
            return jsonPath;
        }

        // 返回相对于父路径的部分
        if (jsonPath.startsWith(parentPath)) {
            String relative = jsonPath.substring(parentPath.length());
            return relative.startsWith(".") ? relative.substring(1) : relative;
        }

        return jsonPath;
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
