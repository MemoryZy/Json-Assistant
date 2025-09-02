package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class CaseSensitiveZAAction extends SortAction {

    public CaseSensitiveZAAction() {
        super(JsonAssistantBundle.messageOnSystem("action.caseSensitiveZA.text"), JsonAssistantBundle.messageOnSystem("action.caseSensitiveZA.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.CASE_SENSITIVE_Z_A;
    }
}
