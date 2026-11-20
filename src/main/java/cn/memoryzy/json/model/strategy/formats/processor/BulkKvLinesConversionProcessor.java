package cn.memoryzy.json.model.strategy.formats.processor;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.model.strategy.formats.context.AbstractGlobalTextConversionProcessor;
import cn.memoryzy.json.model.strategy.formats.data.ActionData;
import cn.memoryzy.json.model.strategy.formats.data.EditorData;
import cn.memoryzy.json.model.strategy.formats.data.MessageData;
import cn.memoryzy.json.util.BulkKvLinesUtil;
import icons.JsonAssistantIcons;

/**
 * Bulk KV Lines（批量键值行格式）转 JSON 的处理器。
 * <p>
 * 该处理器由原来的独立 action（Bulk KV Lines 转 JSON 的右键菜单项）合并而来：
 * 转换逻辑完全复用 {@link BulkKvLinesUtil}，这里只负责「判断能否转换」，
 * 以及向 action 提供名称、描述、图标与转换成功后的提示文案。
 * <p>
 * 文本形态示例（一行一个键值对，用点号与下标表达层级，行首 {@code //} 表示保留但禁用）：
 * <pre>
 * name:John
 * metadata.author:Memory
 * list[0]:1
 * coll[0].actions[0].label:edit
 * // coll[0].actions[1].label:delete
 * </pre>
 *
 * @author Memory
 * @since 2026/11/19
 */
public class BulkKvLinesConversionProcessor extends AbstractGlobalTextConversionProcessor {

    /**
     * @param editorData 编辑器相关信息
     */
    public BulkKvLinesConversionProcessor(EditorData editorData) {
        // 转换出的 JSON 需要格式化，与其它格式（XML / YAML / TOML 等）的行为保持一致
        super(editorData, true);
    }

    /**
     * 判断文本能否按 Bulk KV Lines（批量键值行格式）解析为 JSON。
     * <p>
     * 判定口径见 {@link BulkKvLinesUtil#canBulkBeConvertedToJson(String)}：
     * 首行不是 JSON / JSON5 结构，且每一行（忽略空行与 {@code //} 禁用行）都是合法的「键路径:值」。
     *
     * @param text 待判断文本（选中文本或整篇文档）
     * @return 可以转换返回 true
     */
    @Override
    public boolean canConvert(String text) {
        return BulkKvLinesUtil.canBulkBeConvertedToJson(text);
    }

    /**
     * 执行转换：把 Bulk KV Lines 文本重建为 JSON 文本。
     * <p>
     * 细节（嵌套对象、数组下标补位、禁用行跳过、超长数字按字符串处理等）全部在工具类中完成，
     * 这里不做任何二次处理，避免两处规则不一致。
     *
     * @return 转换后的 JSON 文本；无法解析时返回 null
     */
    @Override
    public String convertToJson() {
        return BulkKvLinesUtil.bulkToJson(getContent());
    }


    // -------------------------- Provide Information -------------------------- //

    /**
     * 提供 action 的名称、描述与图标。
     * <p>
     * 名称与描述复用原有的文案 key，保证菜单项在「其它格式转 JSON」中展示为同一入口时文案不变。
     */
    @Override
    protected ActionData createActionData() {
        return super.createActionData()
                .setActionName(JsonAssistantBundle.message("action.bulk.kv.lines.to.json.text"))
                .setActionDescription(JsonAssistantBundle.messageOnSystem("action.bulk.kv.lines.to.json.description"))
                .setActionIcon(JsonAssistantIcons.FileTypes.KV);
    }

    /**
     * 提供转换成功后的提示文案（选中内容 / 全局内容两种场景）。
     */
    @Override
    protected MessageData createMessageData() {
        return super.createMessageData()
                .setSelectionConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.selection.bulk.kv.lines.to.json"))
                .setGlobalConvertSuccessMessage(JsonAssistantBundle.messageOnSystem("hint.global.bulk.kv.lines.to.json"));
    }

}
