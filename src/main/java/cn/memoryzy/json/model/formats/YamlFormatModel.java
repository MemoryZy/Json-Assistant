package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.YamlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Caret;

import javax.swing.*;

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
        return JsonAssistantBundle.messageOnSystem("hint.select.yaml.to.json.text");
    }

    @Override
    public String getDefaultHint() {
        return JsonAssistantBundle.messageOnSystem("hint.all.yaml.to.json.text");
    }
}
