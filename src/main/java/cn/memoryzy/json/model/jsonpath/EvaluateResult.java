package cn.memoryzy.json.model.jsonpath;

/**
 * @author Memory
 * @since 2024/12/18
 */
public abstract class EvaluateResult {

    private final String message;

    protected EvaluateResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
