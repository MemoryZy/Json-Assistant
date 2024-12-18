package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.jsonpath.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.serialization.ClassUtil;
import com.jayway.jsonpath.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2024/12/18
 */
public class JsonPathEvaluator {

    public static EvaluateResult evaluate(String expression, String jsonDoc, Set<Option> evalOptions) {
        JsonPath jsonPath;
        try {
            if (StrUtil.isBlank(expression)) {
                return null;
            }

            jsonPath = JsonPath.compile(expression);
        } catch (InvalidPathException ex) {
            return new IncorrectExpression(ex.getLocalizedMessage());
        }

        if (StrUtil.isBlank(jsonDoc)) {
            return new IncorrectDocument(JsonAssistantBundle.messageOnSystem("jsonpath.evaluate.doc.blank"));
        }

        Configuration configuration = new Configuration.ConfigurationBuilder()
                .options(evalOptions)
                .build();

        DocumentContext jsonDocument;
        try {
            jsonDocument = JsonPath.parse(jsonDoc, configuration);
        } catch (IllegalArgumentException ex) {
            return new IncorrectDocument(ex.getLocalizedMessage());

        } catch (InvalidJsonException ej) {
            // 检查是否需要解包由json-smart引发的ParseException
            if (ej.getCause() != ej && (ej.getMessage() != null && ej.getMessage().contains("ParseException"))) {
                String message = ej.getCause() != null ? ej.getCause().getLocalizedMessage() : null;
                if (message != null) {
                    return new IncorrectDocument(message);
                }
            }

            return new IncorrectDocument(ej.getLocalizedMessage());
        }

        Object result;
        try {
            result = jsonDocument.read(jsonPath);
        } catch (JsonPathException | IllegalStateException jpe) {
            return new ResultNotFound(jpe.getLocalizedMessage());
        }

        return new ResultString(toResultString(configuration, result));
    }

    private static String toResultString(Configuration config, Object result) {
        if (result == null) return "null";

        if (result instanceof String) {
            return "\"" + StringUtil.escapeStringCharacters((String) result) + "\"";
        }

        if (ClassUtil.isPrimitive(result.getClass())) {
            return result.toString();
        }

        if (result instanceof Collection<?>) {
            // .keys() result is Set<String>
            Collection<?> collection = (Collection<?>) result;
            return "[" + collection.stream()
                    .map(item -> toResultString(config, item))
                    .collect(Collectors.joining(", ")) + "]";
        }

        try {
            return config.jsonProvider().toJson(result);
        } catch (Exception e) {
            return "";
        }
    }

}
