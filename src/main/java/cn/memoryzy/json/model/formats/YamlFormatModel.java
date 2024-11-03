package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.model.YamlDocumentModel;
import cn.memoryzy.json.ui.dialog.MultiYamlDocumentChooser;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Caret;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2024/9/24
 */
public class YamlFormatModel extends BaseFormatModel {

    public YamlFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        super(startOffset, endOffset, primaryCaret, FileTypeEnum.YAML.getFileTypeQualifiedName());
    }

    @Override
    public boolean isConformFormat(String text) {
        return YamlUtil.isYaml(text);
    }

    @Override
    public String convertToJson() {
        String json = YamlUtil.toJson(getContent());
        return StrUtil.isNotBlank(json) ? JsonUtil.formatJson(json) : "";
    }

    @Override
    public String getActionName() {
        return JsonAssistantBundle.message("action.yaml.to.json.text");
    }

    @Override
    public String getActionDescription() {
        return JsonAssistantBundle.messageOnSystem("action.yaml.to.json.description");
    }

    @Override
    public Icon getActionIcon() {
        return AllIcons.FileTypes.Yaml;
    }

    @Override
    public String getSelectHint() {
        return JsonAssistantBundle.messageOnSystem("hint.selection.yaml.to.json.text");
    }

    @Override
    public String getDefaultHint() {
        return JsonAssistantBundle.messageOnSystem("hint.global.yaml.to.json.text");
    }

    @Override
    public void preprocessing() {
        // Yaml文件中，可能存在多份文档，但不能同时转换多份文档为同一Json，故加个选择
        String content = getContent();
        boolean singleYamlDocument = YamlUtil.isSingleYamlDocument(content);
        if (singleYamlDocument) {
            return;
        }

        List<Object> values = YamlUtil.loadAll(content);
        List<Object> collect = values.stream().filter(el -> el instanceof List || el instanceof Map).collect(Collectors.toList());
        // 若有效文档只有一个，则默认将其作转换的文档
        if (collect.size() == 1) {
            Object obj = collect.get(0);
            setContent(JsonUtil.toJsonStr(obj));
        } else {
            MultiYamlDocumentChooser chooser = new MultiYamlDocumentChooser(collect);
            if (chooser.showAndGet()) {
                YamlDocumentModel selectValue = chooser.getSelectValue();
                setContent(JsonUtil.toJsonStr(selectValue.getValue()));
            }
        }
    }
}
