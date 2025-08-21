package cn.memoryzy.json.constant;

import cn.memoryzy.json.JsonAssistantPlugin;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author Memory
 * @since 2024/7/12
 */
public interface PluginConstant {

    /**
     * Json Assistant 工具窗口 ID
     */
    String JSON_ASSISTANT_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.JsonAssistant";

    /**
     * Json Structure 工具窗口 ID
     */
    String AUXILIARY_TREE_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.AuxiliaryTree";

    /**
     * Json 历史记录工具窗口 ID
     */
    String HISTORY_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.JsonHistory";

    /**
     * 选择的软换行状态 Key
     */
    String SOFT_WRAPS_SELECT_STATE = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SOFT_WRAPS_SELECT_STATE";

    String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";
    String JMES_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JmesPathHistory";

    String KOTLIN_TRANSIENT = "kotlin.jvm.Transient";

    String JSON_EXAMPLE = " {\"name\": \"Memory\", \"age\": 18}";

    String JSON_EXAMPLE_COMMENTS = "// {}\n" +
            "{\n" +
            "  // {}\n" +
            "  \"name\": \"Memory\",\n" +
            "  // {}\n" +
            "  \"age\": 18,\n" +
            "  // {}\n" +
            "  \"hobbies\": [\"reading\", \"swimming\", \"coding\"],\n" +
            "  // {}\n" +
            "  \"dateOfBirth\": \"1999-03-19\",\n" +
            "  // {}\n" +
            "  \"address\": {\n" +
            "    // {}\n" +
            "    \"country\": \"China\",\n" +
            "    // {}\n" +
            "    \"province\": \"Guangdong\",\n" +
            "    // {}\n" +
            "    \"city\": \"Guangzhou\",\n" +
            "  }\n" +
            "}";

    String JSON_EXAMPLE_ID = JsonAssistantPlugin.PLUGIN_ID_NAME + "#EXAMPLE";

    String MAIN_WINDOW_DISPLAY_NAME = "View";
    String AUXILIARY_TREE_WINDOW_DISPLAY_NAME = "Tab";

    String HISTORY_EDITOR_NAME = "record";

    String OUTLINE_PROPERTY = "JComponent.outline";
    String ERROR_VALUE = "error";
    String UNKNOWN = "unknown";

    String COMMENT_KEY = "#$__comments__";

    String PROPERTY_COMMENT_TEMPLATE = "/**\n" +
            "* {}\n" +
            "*/";

    String NEW_CLASS_TEMPLATE_NAME = "New Class.java";

    String en_US = "en_US";

    String zh_CN = "zh_CN";

    String MANUAL_SAVE_HISTORY_REMINDER = JsonAssistantPlugin.PLUGIN_ID_NAME + ".MANUAL_SAVE_HISTORY_REMINDER";

    String EXTERNAL_FILE_REMINDER = JsonAssistantPlugin.PLUGIN_ID_NAME + ".EXTERNAL_FILE_REMINDER";


    String[] COLLECTION_FQN = {
            Iterable.class.getName(),
            Collection.class.getName(),
            List.class.getName(),
            Set.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName()
    };

    String[] BIGDECIMAL_FQN = {BigDecimal.class.getName()};

    String[] DATE_TIME_FQN = {Date.class.getName(), LocalDateTime.class.getName()};

    String[] DATE_FQN = {LocalDate.class.getName()};

    String[] TIME_FQN = {LocalTime.class.getName(), Time.class.getName()};

}
