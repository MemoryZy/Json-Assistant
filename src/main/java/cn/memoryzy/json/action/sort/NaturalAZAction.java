package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class NaturalAZAction extends SortAction {

    public NaturalAZAction() {
        super(JsonAssistantBundle.messageOnSystem("action.naturalAZ.text"), JsonAssistantBundle.messageOnSystem("action.naturalAZ.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.NATURAL_ORDER_A_Z;
    }
}
