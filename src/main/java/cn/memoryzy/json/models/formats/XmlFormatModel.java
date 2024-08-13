package cn.memoryzy.json.models.formats;

import cn.memoryzy.json.bundles.JsonAssistantBundle;
import cn.memoryzy.json.utils.XmlUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.fileTypes.FileType;

/**
 * @author Memory
 * @since 2024/8/5
 */
public class XmlFormatModel extends BaseFormatModel {

    public XmlFormatModel(int startOffset, int endOffset, Caret primaryCaret) {
        super(startOffset, endOffset, primaryCaret);
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
        return JsonAssistantBundle.messageOnSystem("action.xml.to.json.description");
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
    public FileType getFileType() {
        return XmlFileType.INSTANCE;
    }
}
