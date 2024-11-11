package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.TextTransformUtil;
import icons.JsonAssistantIcons;

/**
 * @author Memory
 * @since 2024/11/4
 */
public class UrlParamConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public UrlParamConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) throws Exception {
        return null != TextTransformUtil.urlParamsToJson(text);
    }

    @Override
    public String convertToJson() throws Exception {
        return TextTransformUtil.urlParamsToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.url.param.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.url.param.to.json.description"))
                .setActionIcon(JsonAssistantIcons.FileTypes.URL);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.url.param.to.json.text"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.url.param.to.json.text"));
    }

}
