package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.TomlUtil;
import com.intellij.openapi.editor.Caret;
import icons.JsonAssistantIcons;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/9/25
 */
public class TomlFormatModel extends BaseFormatModel {

    public TomlFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        super(startOffset, endOffset, primaryCaret, FileTypeEnum.TOML.getFileTypeQualifiedName());
    }

    @Override
    public boolean isConformFormat(String text) {
        return TomlUtil.isToml(text);
    }

    @Override
    public String convertToJson() {
        String json = TomlUtil.toJson(getContent());
        return StrUtil.isNotBlank(json) ? JsonUtil.formatJson(json) : "";
    }

    @Override
    public String getActionName() {
        return JsonAssistantBundle.message("action.toml.to.json.text");
    }

    @Override
    public String getActionDescription() {
        return JsonAssistantBundle.messageOnSystem("action.toml.to.json.description");
    }

    @Override
    public Icon getActionIcon() {
        return JsonAssistantIcons.FileTypes.TOML;
    }

    @Override
    public String getSelectHint() {
        return JsonAssistantBundle.messageOnSystem("hint.select.toml.to.json.text");
    }

    @Override
    public String getDefaultHint() {
        return JsonAssistantBundle.messageOnSystem("hint.all.toml.to.json.text");
    }

    @Override
    public void preprocessing() {
    }
}
