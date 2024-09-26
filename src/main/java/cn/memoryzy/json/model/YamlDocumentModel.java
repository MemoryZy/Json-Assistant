package cn.memoryzy.json.model;

import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.openapi.editor.actions.ContentChooser;
import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/9/25
 */
public class YamlDocumentModel {

    private final String shortText;
    private final String longText;
    private final Object value;

    public YamlDocumentModel(String shortText, String longText, Object value) {
        this.shortText = shortText;
        this.longText = longText;
        this.value = value;
    }

    public static List<YamlDocumentModel> of(List<Object> values) {
        List<YamlDocumentModel> models = new ArrayList<>();
        for (Object value : values) {
            String yaml = YamlUtil.toYaml(value);
            String truncatedText = JsonAssistantUtil.truncateText(yaml, 70, "...");
            truncatedText = StringUtil.convertLineSeparators(truncatedText, ContentChooser.RETURN_SYMBOL);
            models.add(new YamlDocumentModel(truncatedText, yaml, value));
        }

        return models;
    }

    public String getShortText() {
        return shortText;
    }


    public String getLongText() {
        return longText;
    }


    public Object getValue() {
        return value;
    }


    @Override
    public String toString() {
        return shortText;
    }
}
