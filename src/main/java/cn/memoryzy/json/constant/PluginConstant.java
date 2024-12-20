package cn.memoryzy.json.constant;

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
     * 选择的软换行状态 Key
     */
    String SOFT_WRAPS_SELECT_STATE = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SOFT_WRAPS_SELECT_STATE";

    /**
     * Json 编辑器卡片名称
     */
    String JSON_EDITOR_CARD_NAME = "editor";

    /**
     * Json 树卡片名称
     */
    String JSON_TREE_CARD_NAME = "tree";

    /**
     * JSONQuery 卡片名
     */
    String JSON_QUERY_CARD_NAME = "json_query";

    String KOTLIN_TRANSIENT = "kotlin.jvm.Transient";
    String LOMBOK_LIB = "org.projectlombok:lombok";
    String JSON_EXAMPLE = " {\"name\": \"王铁柱\", \"age\": 18}";

    String JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME = "View";
    String AUXILIARY_TREE_TOOL_WINDOW_DISPLAY_NAME = "Tab";

    String OUTLINE_PROPERTY = "JComponent.outline";
    String ERROR_VALUE = "error";
    String UNKNOWN = "unknown";

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
