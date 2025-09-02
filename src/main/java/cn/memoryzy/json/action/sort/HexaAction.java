package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class HexaAction extends SortAction {

    public HexaAction() {
        super(JsonAssistantBundle.messageOnSystem("action.hexa.text"), JsonAssistantBundle.messageOnSystem("action.hexa.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.HEXA;
    }
}
