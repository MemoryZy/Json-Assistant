package cn.memoryzy.json.action.sort;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.sort.SortStrategies;
import cn.memoryzy.json.model.sort.SortStrategy;

/**
 * @author Memory
 * @since 2025/9/1
 */
public class CaseInsensitiveAZAction extends SortAction {

    public CaseInsensitiveAZAction() {
        super(JsonAssistantBundle.messageOnSystem("action.caseInsensitiveAZ.text"), JsonAssistantBundle.messageOnSystem("action.caseInsensitiveAZ.description"), null);
    }

    @Override
    protected SortStrategy getStrategy() {
        return SortStrategies.CASE_INSENSITIVE_A_Z;
    }
}
