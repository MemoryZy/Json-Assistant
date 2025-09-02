package cn.memoryzy.json.constant;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * @author Memory
 * @since 2024/8/8
 */
public interface ActionHolder {

    interface Main {
        AnAction JSON_BEAUTIFY_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_BEAUTIFY_ACTION_ID);
        AnAction JSON_MINIFY_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_MINIFY_ACTION_ID);
        AnAction JSON_STRUCTURE_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_STRUCTURE_ACTION_ID);
        AnAction JSON_GRID_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_GRID_ACTION_ID);
        AnAction JSON_TEXT_DIFF_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_TEXT_DIFF_ACTION_ID);
        AnAction JSON_ESCAPE_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.JSON_ESCAPE_ACTION_ID);
        AnAction SHORTCUT_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Main.SHORTCUT_ACTION_ID);
    }

    interface Convert {
        AnAction CONVERT_OTHER_FORMATS_GROUP = ActionManager.getInstance().getAction(ActionIdHolder.Convert.CONVERT_OTHER_FORMATS_GROUP_ID);
        AnAction TO_XML_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_XML_ACTION_ID);
        AnAction TO_YAML_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_YAML_ACTION_ID);
        AnAction TO_TOML_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_TOML_ACTION_ID);
        AnAction TO_URL_PARAM_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_URL_PARAM_ACTION_ID);
        AnAction TO_PROPERTIES_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_PROPERTIES_ACTION_ID);
        AnAction TO_JSON5_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_JSON5_ACTION_ID);
        AnAction TO_JSON_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Convert.TO_JSON_ACTION_ID);
    }

    interface Extend {
        AnAction EXTEND_GROUP = ActionManager.getInstance().getAction(ActionIdHolder.Extend.EXTEND_GROUP_ID);
        AnAction CONVERT_ALL_TIMESTAMP_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Extend.CONVERT_ALL_TIMESTAMP_ACTION_ID);
        AnAction EXPAND_ALL_NESTED_JSON_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Extend.EXPAND_ALL_NESTED_JSON_ACTION_ID);
        AnAction CONVERT_ALL_READABLE_TIME_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Extend.CONVERT_ALL_READABLE_TIME_ACTION_ID);

        AnAction FILL_COMMENT_FROM_JAVA_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Extend.FILL_COMMENT_FROM_JAVA_ACTION_ID);
    }

    interface Sort {
        AnAction SORT_GROUP = ActionManager.getInstance().getAction(ActionIdHolder.Sort.SORT_GROUP_ID);

        AnAction CASE_SENSITIVE_AZ_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.CASE_SENSITIVE_AZ_ACTION_ID);
        AnAction CASE_SENSITIVE_ZA_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.CASE_SENSITIVE_ZA_ACTION_ID);
        AnAction CASE_INSENSITIVE_AZ_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.CASE_INSENSITIVE_AZ_ACTION_ID);
        AnAction CASE_INSENSITIVE_ZA_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.CASE_INSENSITIVE_ZA_ACTION_ID);
        AnAction NATURAL_AZ_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.NATURAL_AZ_ACTION_ID);
        AnAction NATURAL_ZA_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.NATURAL_ZA_ACTION_ID);
        AnAction HEXA_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.HEXA_ACTION_ID);
        AnAction REVERSE_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.REVERSE_ACTION_ID);
        AnAction SHUFFLE_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.SHUFFLE_ACTION_ID);
        AnAction LENGTH_ASC_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.LENGTH_ASC_ACTION_ID);
        AnAction LENGTH_DESC_ACTION = ActionManager.getInstance().getAction(ActionIdHolder.Sort.LENGTH_DESC_ACTION_ID);

    }

    interface Float {
        AnAction EDITOR_FLOAT_GROUP = ActionManager.getInstance().getAction(ActionIdHolder.Float.EDITOR_FLOAT_GROUP_ID);
        AnAction HISTORY_FLOAT_GROUP = ActionManager.getInstance().getAction(ActionIdHolder.Float.HISTORY_FLOAT_GROUP_ID);
    }
}
