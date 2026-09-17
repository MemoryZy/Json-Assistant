package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class CaseInsensitiveZAAction extends SortAction {

    public CaseInsensitiveZAAction() {
        super(JsonAssistantBundle.messageOnSystem("action.caseInsensitiveZA.text"), JsonAssistantBundle.messageOnSystem("action.caseInsensitiveZA.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.CASE_INSENSITIVE_Z_A;
    }
}
