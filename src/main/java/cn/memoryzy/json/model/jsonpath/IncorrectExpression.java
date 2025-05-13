package cn.memoryzy.json.model.jsonpath;

/**
 * 非法表达式
 *
 * @author Memory
 * @since 2024/12/18
 */
public class IncorrectExpression extends EvaluateResult {

    public IncorrectExpression(String message) {
        super(message);
    }

}
