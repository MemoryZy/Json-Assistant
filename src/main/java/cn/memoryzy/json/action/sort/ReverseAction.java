package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class ReverseAction extends SortAction {

    public ReverseAction() {
        super(JsonAssistantBundle.messageOnSystem("action.reverse.text"), JsonAssistantBundle.messageOnSystem("action.reverse.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.REVERSE;
    }
}
