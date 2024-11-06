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

    String MAIN_ACTION_ID = "JsonAssistant.Action.Main";
    String JSON_ASSISTANT_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.JsonAssistant";

    String KOTLIN_TRANSIENT = "kotlin.jvm.Transient";
    String LOMBOK_LIB = "org.projectlombok:lombok";
    String JSON_EXAMPLE = " {\"name\": \"王铁柱\", \"age\": 18}";

    String JSON_ASSISTANT_TOOL_WINDOW_DISPLAY_NAME = "View";

    String OUTLINE_PROPERTY = "JComponent.outline";
    String ERROR_VALUE = "error";

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
