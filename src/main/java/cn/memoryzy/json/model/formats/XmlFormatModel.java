package cn.memoryzy.json.model.formats;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.util.JsonUtil;
import cn.memoryzy.json.util.XmlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Caret;

import javax.swing.*;

/**
 * @author Memory
 * @since 2024/8/5
 */
public class XmlFormatModel extends BaseFormatModel {

    public XmlFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        super(startOffset, endOffset, primaryCaret, FileTypeEnum.XML.getFileTypeQualifiedName());
    }

    @Override
    public boolean isConformFormat(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson() {
        String json = XmlUtil.toJson(getContent());
        return StrUtil.isNotBlank(json) ? JsonUtil.formatJson(json) : "";
    }

    @Override
    public String getActionName() {
        return JsonAssistantBundle.message("action.xml.to.json.text");
    }

    @Override
    public String getActionDescription() {
        return JsonAssistantBundle.messageOnSystem("action.xml.to.json.description");
    }

    @Override
    public Icon getActionIcon() {
        return AllIcons.FileTypes.Xml;
    }

    @Override
    public String getSelectHint() {
        return JsonAssistantBundle.messageOnSystem("hint.select.xml.to.json.text");
    }

    @Override
    public String getDefaultHint() {
        return JsonAssistantBundle.messageOnSystem("hint.all.xml.to.json.text");
    }

    @Override
    public void preprocessing() {
    }
}
