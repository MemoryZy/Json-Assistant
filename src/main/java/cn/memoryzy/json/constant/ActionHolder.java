package cn.memoryzy.json.constant;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * @author Memory
 * @since 2024/8/8
 */
public interface ActionHolder {

    String MAIN_ACTION_ID = "JsonAssistant.Action.Main";

    String JSON_BEAUTIFY_ACTION_ID = "JsonAssistant.Action.JsonBeautifyAction";
    String JSON_MINIFY_ACTION_ID = "JsonAssistant.Action.JsonMinifyAction";
    String JSON_STRUCTURE_ACTION_ID = "JsonAssistant.Action.JsonStructureAction";
    String JSON_TEXT_DIFF_ACTION_ID = "JsonAssistant.Action.JsonTextDiffAction";
    String CONVERT_OTHER_FORMATS_GROUP_ID = "JsonAssistant.Group.ConvertOtherFormatsGroup";
    String SHORTCUT_ACTION_ID = "JsonAssistant.Action.ShortcutAction";
    String JSON_ESCAPE_ACTION_ID = "JsonAssistant.Action.JsonEscapeAction";
    String CONVERT_ALL_TIMESTAMP_ACTION_ID = "JsonAssistant.Action.ConvertAllTimestampAction";
    String EXPAND_ALL_NESTED_JSON_ACTION_ID = "JsonAssistant.Action.ExpandAllNestedJsonAction";
    String CONVERT_ALL_READABLE_TIME_ACTION_ID = "JsonAssistant.Action.ConvertAllReadableTimeAction";
    String EXTEND_GROUP_ID = "JsonAssistant.Group.ExtendGroup";
    String TO_XML_ACTION_ID = "JsonAssistant.Action.ToXmlAction";
    String TO_YAML_ACTION_ID = "JsonAssistant.Action.ToYamlAction";
    String TO_TOML_ACTION_ID = "JsonAssistant.Action.ToTomlAction";
    String TO_URL_PARAM_ACTION_ID = "JsonAssistant.Action.ToUrlParamAction";
    String TO_PROPERTIES_ACTION_ID = "JsonAssistant.Action.ToPropertiesAction";
    String TO_JSON5_ACTION_ID = "JsonAssistant.Action.ToJson5Action";
    String TO_JSON_ACTION_ID = "JsonAssistant.Action.ToJsonAction";

    AnAction JSON_BEAUTIFY_ACTION = ActionManager.getInstance().getAction(JSON_BEAUTIFY_ACTION_ID);
    AnAction JSON_MINIFY_ACTION = ActionManager.getInstance().getAction(JSON_MINIFY_ACTION_ID);
    AnAction JSON_STRUCTURE_ACTION = ActionManager.getInstance().getAction(JSON_STRUCTURE_ACTION_ID);
    AnAction JSON_TEXT_DIFF_ACTION = ActionManager.getInstance().getAction(JSON_TEXT_DIFF_ACTION_ID);
    AnAction CONVERT_OTHER_FORMATS_GROUP = ActionManager.getInstance().getAction(CONVERT_OTHER_FORMATS_GROUP_ID);
    AnAction SHORTCUT_ACTION = ActionManager.getInstance().getAction(SHORTCUT_ACTION_ID);
    AnAction JSON_ESCAPE_ACTION = ActionManager.getInstance().getAction(JSON_ESCAPE_ACTION_ID);
    AnAction CONVERT_ALL_TIMESTAMP_ACTION = ActionManager.getInstance().getAction(CONVERT_ALL_TIMESTAMP_ACTION_ID);
    AnAction EXPAND_ALL_NESTED_JSON_ACTION = ActionManager.getInstance().getAction(EXPAND_ALL_NESTED_JSON_ACTION_ID);
    AnAction CONVERT_ALL_READABLE_TIME_ACTION = ActionManager.getInstance().getAction(CONVERT_ALL_READABLE_TIME_ACTION_ID);
    AnAction EXTEND_GROUP = ActionManager.getInstance().getAction(EXTEND_GROUP_ID);
    AnAction TO_XML_ACTION = ActionManager.getInstance().getAction(TO_XML_ACTION_ID);
    AnAction TO_YAML_ACTION = ActionManager.getInstance().getAction(TO_YAML_ACTION_ID);
    AnAction TO_TOML_ACTION = ActionManager.getInstance().getAction(TO_TOML_ACTION_ID);
    AnAction TO_URL_PARAM_ACTION = ActionManager.getInstance().getAction(TO_URL_PARAM_ACTION_ID);
    AnAction TO_PROPERTIES_ACTION = ActionManager.getInstance().getAction(TO_PROPERTIES_ACTION_ID);
    AnAction TO_JSON5_ACTION = ActionManager.getInstance().getAction(TO_JSON5_ACTION_ID);
    AnAction TO_JSON_ACTION = ActionManager.getInstance().getAction(TO_JSON_ACTION_ID);

}
