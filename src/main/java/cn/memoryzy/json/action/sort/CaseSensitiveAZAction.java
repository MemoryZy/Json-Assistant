package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class CaseSensitiveAZAction extends SortAction {

    public CaseSensitiveAZAction() {
        super(JsonAssistantBundle.messageOnSystem("action.caseSensitiveAZ.text"), JsonAssistantBundle.messageOnSystem("action.caseSensitiveAZ.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.CASE_SENSITIVE_A_Z;
    }
}
