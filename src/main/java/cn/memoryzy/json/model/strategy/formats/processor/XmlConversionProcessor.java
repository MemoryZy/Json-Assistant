package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.data.ActionData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.MessageData;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.util.XmlUtil;
import com.intellij.icons.AllIcons;

/**
 * @author Memory
 * @since 2024/11/2
 */
public class XmlConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public XmlConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        return XmlUtil.isXML(text);
    }

    @Override
    public String convertToJson() {
        return XmlUtil.toJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.xml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.xml.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Xml);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.xml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.xml.to.json.text"));
    }

}
