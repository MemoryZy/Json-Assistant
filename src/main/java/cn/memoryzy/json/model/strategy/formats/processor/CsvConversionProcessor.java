package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.CsvUtil;
import com.intellij.icons.AllIcons;

/**
 * CSV → JSON 转换策略处理器。
 * <p>默认启用类型推断；由 {@link cn.memoryzy.json.action.OtherFormatsToJsonAction} 自动识别触发。</p>
 *
 * @author Memory
 * @since 2026/09/16
 */
public class CsvConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public CsvConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        return CsvUtil.canCsvBeConvertedToJson(text);
    }

    @Override
    public String convertToJson() {
        return CsvUtil.csvToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.csv.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.csv.to.json.description"))
                .setActionIcon(AllIcons.FileTypes.Text);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.csv.to.json"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.csv.to.json"));
    }

}