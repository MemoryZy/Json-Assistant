package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class LengthDescAction extends SortAction {

    public LengthDescAction() {
        super(JsonAssistantBundle.messageOnSystem("action.length.desc.text"), JsonAssistantBundle.messageOnSystem("action.length.desc.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.LINE_LENGTH_LONG_SHORT;
    }
}
