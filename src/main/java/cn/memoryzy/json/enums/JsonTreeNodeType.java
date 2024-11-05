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
    JSONObjectEl,

    /**
     * 数组下的基本类型
     */
    JSONArrayEl,

    /**
     * 普通对象下的普通类型
     */
    JSONObjectKey

}
