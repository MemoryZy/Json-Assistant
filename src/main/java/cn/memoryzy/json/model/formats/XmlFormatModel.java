package cn.memoryzy.json.model.formats;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.XmlUtil;
import com.intellij.openapi.editor.Caret;

/**
 * @author Memory
 * @since 2024/8/5
 */
public class XmlFormatModel extends BaseFormatModel {

    public XmlFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        super(startOffset, endOffset, primaryCaret);
    }

    public XmlFormatModel(Boolean isSelected, int startOffset, int endOffset, Caret primaryCaret, String content) {
        super(isSelected, startOffset, endOffset, primaryCaret, content);
    }

    @Override
    public boolean isConformFormat(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson() {
        return XmlUtil.xmlToJson(getContent());
    }

    @Override
    public String getActionName() {
        return JsonAssistantBundle.message("action.xml.to.json.text");
    }

    @Override
    public String getActionDescription() {
        return JsonAssistantBundle.message("action.xml.to.json.description");
    }

    @Override
    public String getSelectHint() {
        return JsonAssistantBundle.messageOnSystem("hint.select.xml.to.json.text");
    }

    @Override
    public String getDefaultHint() {
        return JsonAssistantBundle.messageOnSystem("hint.all.xml.to.json.text");
    }
}
