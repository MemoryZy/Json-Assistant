package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class NaturalZAAction extends SortAction {

    public NaturalZAAction() {
        super(JsonAssistantBundle.messageOnSystem("action.naturalZA.text"), JsonAssistantBundle.messageOnSystem("action.naturalZA.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.NATURAL_ORDER_Z_A;
    }
}
