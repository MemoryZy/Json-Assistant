package cn.memoryzy.json.constant;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * @author Memory
 * @since 2024/8/8
 */
public class ActionHolder {

    public static final String JSON_BEAUTIFY_ACTION_ID = "JsonAssistant.Action.JsonBeautifyAction";
    public static final String JSON_MINIFY_ACTION_ID = "JsonAssistant.Action.JsonMinifyAction";
    public static final String JSON_STRUCTURE_ACTION_ID = "JsonAssistant.Action.JsonStructureAction";
    public static final String CONVERT_OTHER_FORMATS_GROUP_ID = "JsonAssistant.Group.ConvertOtherFormatsGroup";
    public static final String SHORTCUT_ACTION_ID = "JsonAssistant.Action.ShortcutAction";
    public static final String ONLINE_DOC_ACTION_ID = "JsonAssistant.Action.OnlineDocAction";


    public static final AnAction JSON_BEAUTIFY_ACTION = ActionManager.getInstance().getAction(JSON_BEAUTIFY_ACTION_ID);
    public static final AnAction JSON_MINIFY_ACTION = ActionManager.getInstance().getAction(JSON_MINIFY_ACTION_ID);
    public static final AnAction JSON_STRUCTURE_ACTION = ActionManager.getInstance().getAction(JSON_STRUCTURE_ACTION_ID);
    public static final AnAction CONVERT_OTHER_FORMATS_GROUP = ActionManager.getInstance().getAction(CONVERT_OTHER_FORMATS_GROUP_ID);
    public static final AnAction SHORTCUT_ACTION = ActionManager.getInstance().getAction(SHORTCUT_ACTION_ID);
    public static final AnAction ONLINE_DOC_ACTION = ActionManager.getInstance().getAction(ONLINE_DOC_ACTION_ID);

}
