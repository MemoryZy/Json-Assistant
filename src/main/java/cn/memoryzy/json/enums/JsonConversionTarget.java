package cn.memoryzy.json.enums;

import cn.memoryzy.json.bundle.JsonAssistantBundle;

/**
 * @author Memory
 * @since 2025/04/15
 */
public enum JsonConversionTarget {

    /**
     * 当前类
     */
    CURRENT_CLASS("action.serialize.text", "action.serialize.description", "action.serialize.json5.text","action.serialize.json5.description"),

    /**
     * 被引用的外部类（如：userService.convert(UserDTO) 中的 UserDTO）
     */
    REFERENCED_CLASS("action.serialize.class.text", "action.serialize.class.description", "action.serialize.json5.class.text", "action.serialize.json5.class.description"),

    /**
     * 方法内的局部变量（如：UserDTO dto = new UserDTO()）
     */
    LOCAL_VARIABLE("action.serialize.var.text", "action.serialize.var.description", "action.serialize.json5.var.text", "action.serialize.json5.var.description"),

    /**
     * 类成员字段（如：private UserDTO defaultUser）
     */
    CLASS_FIELD("action.serialize.field.text", "action.serialize.field.description", "action.serialize.json5.field.text", "action.serialize.json5.field.description"),

    /**
     * 方法参数（如：public void update(@RequestBody UserDTO dto)）
     */
    METHOD_PARAMETER("action.serialize.param.text", "action.serialize.param.description", "action.serialize.json5.param.text", "action.serialize.json5.param.description");


    private final String jsonName;
    private final String jsonDescription;
    private final String json5Name;
    private final String json5Description;

    JsonConversionTarget(String jsonName, String jsonDescription, String json5Name, String json5Description) {
        this.jsonName = jsonName;
        this.jsonDescription = jsonDescription;
        this.json5Name = json5Name;
        this.json5Description = json5Description;
    }

    public String getJsonName() {
        return JsonAssistantBundle.message(jsonName);
    }

    public String getJsonDescription() {
        return JsonAssistantBundle.messageOnSystem(jsonDescription);
    }

    public String getJson5Name() {
        return JsonAssistantBundle.message(json5Name);
    }

    public String getJson5Description() {
        return JsonAssistantBundle.messageOnSystem(json5Description);
    }
}