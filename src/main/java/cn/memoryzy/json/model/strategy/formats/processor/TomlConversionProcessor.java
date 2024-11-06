package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.data.ActionData;
import cn.memoryzy.json.model.data.EditorData;
import cn.memoryzy.json.model.data.MessageData;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.util.TomlUtil;
import icons.JsonAssistantIcons;

/**
 * @author Memory
 * @since 2024/11/3
 */
public class TomlConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public TomlConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        return TomlUtil.isToml(text);
    }

    @Override
    public String convertToJson() {
        return TomlUtil.toJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.toml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.toml.to.json.description"))
                .setActionIcon(JsonAssistantIcons.FileTypes.TOML);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.toml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.toml.to.json.text"));
    }

}
