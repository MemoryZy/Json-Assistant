package cn.memoryzy.json.model;

import cn.memoryzy.json.util.JsonAssistantUtil;
import cn.memoryzy.json.util.YamlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2024/9/25
 */
public class YamlDocumentModel {

    private String abbreviatedContent;

    private String wholeContent;

    private Object value;

    public YamlDocumentModel(String abbreviatedContent, String wholeContent, Object value) {
        this.abbreviatedContent = abbreviatedContent;
        this.wholeContent = wholeContent;
        this.value = value;
    }

    public static List<YamlDocumentModel> of(List<Object> values) {
        List<YamlDocumentModel> models = new ArrayList<>();
        for (Object value : values) {
            String yaml = YamlUtil.toYaml(value);
            String abbreviatedContent = JsonAssistantUtil.truncateText(yaml, 45, "...");
            models.add(new YamlDocumentModel(abbreviatedContent, yaml, value));
        }

        return models;
    }

    public String getAbbreviatedContent() {
        return abbreviatedContent;
    }

    public void setAbbreviatedContent(String abbreviatedContent) {
        this.abbreviatedContent = abbreviatedContent;
    }

    public String getWholeContent() {
        return wholeContent;
    }

    public void setWholeContent(String wholeContent) {
        this.wholeContent = wholeContent;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return abbreviatedContent;
    }
}
