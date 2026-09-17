package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class LengthAscAction extends SortAction {

    public LengthAscAction() {
        super(JsonAssistantBundle.messageOnSystem("action.length.asc.text"), JsonAssistantBundle.messageOnSystem("action.length.asc.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.LINE_LENGTH_SHORT_LONG;
    }
}
