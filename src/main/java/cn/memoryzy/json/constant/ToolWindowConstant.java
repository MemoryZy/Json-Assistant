package cn.memoryzy.json.constant;

import cn.memoryzy.json.JsonAssistantPlugin;

/**
 * @author Memory
 * @since 2025/12/29
 */
public interface ToolWindowConstant {

    interface Main {
        /**
         * Json Assistant 工具窗口 ID
         */
        String JSON_ASSISTANT_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.JsonAssistant";
        /**
         * 选择的软换行状态 Key
         */
        String SOFT_WRAPS_SELECT_STATE = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SOFT_WRAPS_SELECT_STATE";

        String MAIN_WINDOW_DISPLAY_NAME = "View";
    }

    interface History {
        /**
         * Json 历史记录工具窗口 ID
         */
        String HISTORY_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.JsonHistory";

        /**
         * 历史记录软换行状态
         */
        String SOFT_WRAPS_HISTORY_SELECT_STATE = JsonAssistantPlugin.PLUGIN_ID_NAME + ".SOFT_WRAPS_HISTORY_SELECT_STATE";
    }

    interface Structure {
        /**
         * Json Structure 工具窗口 ID
         */
        String AUXILIARY_TREE_TOOLWINDOW_ID = "JsonAssistant.ToolWindow.AuxiliaryTree";
        String AUXILIARY_TREE_WINDOW_DISPLAY_NAME = "Tab";
    }

    interface Search {
        String JSON_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JsonPathHistory";
        String JMES_PATH_HISTORY_KEY = JsonAssistantPlugin.PLUGIN_ID_NAME + ".JmesPathHistory";
    }

}
