package cn.memoryzy.json.util;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.jsonpath.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.burt.jmespath.parser.ParseException;

/**
 * @author Memory
 * @since 2025/1/16
 */
public class JmesPathEvaluator {

    public static EvaluateResult evaluate(String expressionPath, String jsonDoc) {
        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        Expression<JsonNode> expression;

        try {
            expression = jmespath.compile(expressionPath);
        } catch (ParseException e) {
            return new IncorrectExpression(JsonAssistantBundle.messageOnSystem("json.query.invalid.jmespath.expression"));
        }

        JsonNode input;
        try {
            input = JsonUtil.MAPPER.readTree(jsonDoc);
        } catch (JsonProcessingException e) {
            return new IncorrectDocument(JsonAssistantBundle.messageOnSystem("json.query.invalid.document"));
        }

        // 计算结果
        JsonNode result = expression.search(input);
        String resultStr;

        try {
            resultStr = JsonUtil.MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return new ResultNotFound(JsonAssistantBundle.messageOnSystem("json.query.unable.process.result") + e.getMessage());
        }

        return new ResultString(resultStr);
    }

}
