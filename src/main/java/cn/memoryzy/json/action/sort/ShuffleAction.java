package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class ShuffleAction extends SortAction {

    public ShuffleAction() {
        super(JsonAssistantBundle.messageOnSystem("action.shuffle.text"), JsonAssistantBundle.messageOnSystem("action.shuffle.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.SHUFFLE;
    }
}
