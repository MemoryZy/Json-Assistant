package cn.memoryzy.json.constant;

/**
 * @author Memory
 * @since 2025/9/1
 */
public interface ActionIdHolder {

    String MAIN_ACTION_ID = "JsonAssistant.Action.Main";

    interface Main {
        String JSON_BEAUTIFY_ACTION_ID = "JsonAssistant.Action.JsonBeautifyAction";
        String JSON_MINIFY_ACTION_ID = "JsonAssistant.Action.JsonMinifyAction";
        String JSON_STRUCTURE_ACTION_ID = "JsonAssistant.Action.JsonStructureAction";
        String JSON_GRID_ACTION_ID = "JsonAssistant.Action.JsonGridAction";
        String JSON_TEXT_DIFF_ACTION_ID = "JsonAssistant.Action.JsonTextDiffAction";
        String JSON_ESCAPE_ACTION_ID = "JsonAssistant.Action.JsonEscapeAction";
        String SHORTCUT_ACTION_ID = "JsonAssistant.Action.ShortcutAction";
    }

    interface Convert {
        String CONVERT_OTHER_FORMATS_GROUP_ID = "JsonAssistant.Group.ConvertOtherFormatsGroup";
        String TO_XML_ACTION_ID = "JsonAssistant.Action.ToXmlAction";
        String TO_YAML_ACTION_ID = "JsonAssistant.Action.ToYamlAction";
        String TO_TOML_ACTION_ID = "JsonAssistant.Action.ToTomlAction";
        String TO_URL_PARAM_ACTION_ID = "JsonAssistant.Action.ToUrlParamAction";
        String TO_PROPERTIES_ACTION_ID = "JsonAssistant.Action.ToPropertiesAction";
        String TO_JSON5_ACTION_ID = "JsonAssistant.Action.ToJson5Action";
        String TO_JSON_ACTION_ID = "JsonAssistant.Action.ToJsonAction";
    }

    interface Extend {
        String EXTEND_GROUP_ID = "JsonAssistant.Group.ExtendGroup";
        String CONVERT_ALL_TIMESTAMP_ACTION_ID = "JsonAssistant.Action.ConvertAllTimestampAction";
        String EXPAND_ALL_NESTED_JSON_ACTION_ID = "JsonAssistant.Action.ExpandAllNestedJsonAction";
        String CONVERT_ALL_READABLE_TIME_ACTION_ID = "JsonAssistant.Action.ConvertAllReadableTimeAction";

        String FILL_COMMENT_FROM_JAVA_ACTION_ID = "JsonAssistant.Action.FillCommentFromJavaAction";
    }

    interface Sort {
        String SORT_GROUP_ID = "JsonAssistant.Group.SortGroup";

        String CASE_SENSITIVE_AZ_ACTION_ID = "JsonAssistant.Action.CaseSensitiveAZAction";
        String CASE_SENSITIVE_ZA_ACTION_ID = "JsonAssistant.Action.CaseSensitiveZAAction";
        String CASE_INSENSITIVE_AZ_ACTION_ID = "JsonAssistant.Action.CaseInsensitiveAZAction";
        String CASE_INSENSITIVE_ZA_ACTION_ID = "JsonAssistant.Action.CaseInsensitiveZAAction";

    }

    interface Float {
        String EDITOR_FLOAT_GROUP_ID = "JsonAssistant.Group.PasteEditorFloatGroup";
        String HISTORY_FLOAT_GROUP_ID = "JsonAssistant.Group.HistoryFloatGroup";
    }

}
