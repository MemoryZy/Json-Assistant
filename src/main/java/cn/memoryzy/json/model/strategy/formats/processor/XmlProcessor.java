package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.MessageInfo;
import cn.memoryzy.json.model.strategy.formats.context.AbstractConversionProcessor;
import cn.memoryzy.json.util.XmlUtil;
import com.intellij.icons.AllIcons;

/**
 * @author Memory
 * @since 2024/11/2
 */
public class XmlProcessor extends AbstractConversionProcessor {

    public XmlProcessor(EditorInfo editorInfo) {
        super(editorInfo, true);
    }

    @Override
    public boolean canConvert(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convert() {
        return XmlUtil.toJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionInfo createActionInfo() {
        return super.createActionInfo()
                .setActionName(JsonAssistantBundle.message("action.xml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.xml.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Xml);
    }

    @Override
    protected MessageInfo createMessageInfo() {
        return super.createMessageInfo()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.xml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.xml.to.json.text"));
    }

}
