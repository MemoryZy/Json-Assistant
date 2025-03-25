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
    JSONObjectProperty

}
