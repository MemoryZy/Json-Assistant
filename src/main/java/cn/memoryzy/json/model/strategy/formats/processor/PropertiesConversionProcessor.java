package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.DataConverter;
import com.intellij.icons.AllIcons;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class PropertiesConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public PropertiesConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        // TODO 判断是否处于Properties内


        return DataConverter.canPropertiesBeConvertedToJson(text);
    }

    @Override
    public String convertToJson() {
        return DataConverter.propertiesToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.properties.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.properties.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Properties);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.properties.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.properties.to.json.text"));
    }

}
