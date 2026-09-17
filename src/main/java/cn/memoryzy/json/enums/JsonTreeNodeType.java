package cn.memoryzy.json.enums;

/**
 * @author Memory
 * @since 2024/2/28
 */
public enum JsonTreeNodeType {

    /**
     * 对象类型
     */
    JSONObject,

    /**
     * 数组类型
     */
    JSONArray,

    /**
     * 数组下的对象类型
     */
    JSONObjectElement,

    /**
     * 数组下的数组类型
     */
    JSONArrayElementArray,

    /**
     * 数组下的基本类型
     */
    JSONArrayElement,

    /**
     * 普通对象下的普通类型
     */
    JSONObjectProperty;


    /**
     * 是否为对象/数组节点
     *
     * @param nodeType 节点类型
     * @return 为对象/数组节点，为true；反之为false
     */
    public static boolean isParentNode(JsonTreeNodeType nodeType) {
        return JSONObject == nodeType
                || JSONArray == nodeType
                || JSONObjectElement == nodeType
                || JSONArrayElementArray == nodeType;
    }

    /**
     * 是否为叶子节点（底层节点）
     *
     * @param nodeType 节点类型
     * @return 为叶子节点，为true；反之为false
     */
    public static boolean isLeafNode(JsonTreeNodeType nodeType) {
        return JSONArrayElement == nodeType || JSONObjectProperty == nodeType;
    }

}
