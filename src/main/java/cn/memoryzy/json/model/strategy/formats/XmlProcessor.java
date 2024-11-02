package cn.memoryzy.json.model.strategy.formats;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.enums.FileTypeEnum;
import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.MessageInfo;
import cn.memoryzy.json.util.XmlUtil;
import com.intellij.icons.AllIcons;

/**
 * @author Memory
 * @since 2024/11/2
 */
public class XmlProcessor extends AbstractConversionProcessor {

    public XmlProcessor(EditorInfo editorInfo) {
        super(editorInfo, FileTypeEnum.XML.getFileTypeQualifiedName());
    }

    @Override
    public boolean canConvert(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson() throws Exception {
        return XmlUtil.toJson(getContent());
    }


    // -------------------------- General Method -------------------------- //

    @Override
    protected ActionInfo createActionInfo() {
        return new ActionInfo()
                .setActionName(JsonAssistantBundle.message("action.xml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.xml.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Xml);
    }

    @Override
    protected MessageInfo createMessageInfo() {
        return new MessageInfo()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.xml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.xml.to.json.text"));
    }
}
