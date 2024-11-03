package cn.memoryzy.json.model.strategy.formats;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.formats.ActionInfo;
import cn.memoryzy.json.model.formats.EditorInfo;
import cn.memoryzy.json.model.formats.MessageInfo;
import cn.memoryzy.json.model.strategy.formats.context.AbstractConversionProcessor;
import cn.memoryzy.json.util.TomlUtil;
import icons.JsonAssistantIcons;

/**
 * @author Memory
 * @since 2024/11/3
 */
public class TomlProcessor extends AbstractConversionProcessor {

    public TomlProcessor(EditorInfo editorInfo) {
        super(editorInfo, true);
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
    protected ActionInfo createActionInfo() {
        return new ActionInfo()
                .setActionName(JsonAssistantBundle.message("action.toml.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.toml.to.json.description"))
                .setActionIcon(JsonAssistantIcons.FileTypes.TOML);
    }

    @Override
    protected MessageInfo createMessageInfo() {
        return new MessageInfo()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.toml.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.toml.to.json.text"));
    }

}
