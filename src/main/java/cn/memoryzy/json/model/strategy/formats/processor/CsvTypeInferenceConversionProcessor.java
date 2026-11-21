package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.CsvUtil;
import com.intellij.icons.AllIcons;
import icons.JsonAssistantIcons;

/**
 * CSV → JSON 转换策略处理器（带类型推断）。
 * <p>
 * 转换时会把单元格按字面量推断为数字 / 布尔 / null，其余仍按字符串处理，
 * 与「CSV 转 JSON（字符串）」那个不推断类型的入口区分开；
 * 由 {@link cn.memoryzy.json.action.OtherFormatsToJsonAction} 自动识别触发。
 *
 * @author Memory
 * @since 2026/09/16
 */
public class CsvTypeInferenceConversionProcessor extends AbstractGlobalTextConversionProcessor {

    public CsvTypeInferenceConversionProcessor(EditorData editorData) {
        super(editorData, true);
    }

    @Override
    public boolean canConvert(String text) {
        return CsvUtil.canCsvBeConvertedToJson(text);
    }

    /**
     * 执行转换：使用带类型推断的重载（等价于 {@code csvToJson(csv, true)}）。
     *
     * @return 转换后的 JSON 文本；无法解析时返回 null
     */
    @Override
    public String convertToJson() {
        return CsvUtil.csvToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    /**
     * 提供 action 的名称、描述与图标。
     * <p>
     * 文案单独使用「推断类型」的一组 key，避免复用「不推断类型」入口的文案而误导用户。
     */
    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.csv.to.json.inference.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.csv.to.json.inference.description"))
                .setActionIcon(JsonAssistantIcons.FileTypes.CSV);
    }

    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.csv.to.json"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.csv.to.json"));
    }

}