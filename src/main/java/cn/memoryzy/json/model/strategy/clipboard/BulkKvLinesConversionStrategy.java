package cn.memoryzy.json.model.strategy.clipboard;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.DataTypeConstant;
import cn.memoryzy.json.model.strategy.clipboard.context.ClipboardTextConversionStrategy;
import cn.memoryzy.json.util.BulkKvLinesUtil;

/**
 * 将剪贴板中的 Bulk KV Lines（批量键值行格式）文本转换为 JSON。
 * <p>
 * 该策略只做两件事：判断剪贴板内容能否按 Bulk KV Lines 解析、调用工具类完成转换，
 * 具体规则（点号与下标表达层级、{@code //} 禁用行、值类型推断等）都在 {@link BulkKvLinesUtil} 中。
 * <p>
 * 注意：{@code key: value} 这种带空格的写法同时也是合法 YAML，
 * 因此该策略在 {@code ClipboardTextConversionContext#getProcessors()} 中排在 YAML 之后，
 * 由列表顺序保证「先按 YAML 解析，YAML 不成立时才按 Bulk KV Lines 处理」。
 *
 * @author Memory
 * @since 2026/11/19
 */
public class BulkKvLinesConversionStrategy implements ClipboardTextConversionStrategy {

    @Override
    public String type() {
        return DataTypeConstant.BULK_KV_LINES;
    }

    @Override
    public boolean canConvert(String text) {
        // 先做结构判断：每行都得是合法的「键:值」行，JSON 等结构化文本直接排除
        if (!BulkKvLinesUtil.canBulkBeConvertedToJson(text)) {
            return false;
        }

        // 再做内容判断，避免把「Note: 记得提交」这类普通文本误判成 Bulk KV Lines：
        // 至少两行，或者虽然只有一行但带数组下标（例如 a[0]:1），后者几乎不可能是普通文本
        return hasEnoughRows(text);
    }

    @Override
    public String convertToJson(String text) {
        return BulkKvLinesUtil.bulkToJson(text);
    }

    /**
     * 判断有效行数是否足以认定为 Bulk KV Lines 文本
     *
     * @param text 剪贴板文本
     * @return 有效行数大于等于 2，或存在带下标路径的行时返回 true
     */
    private boolean hasEnoughRows(String text) {
        int rows = 0;
        boolean hasIndexToken = false;

        for (String line : text.split("\\r?\\n")) {
            String trimmed = StrUtil.trim(line);
            // 空行与 // 禁用行不计入判断
            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                continue;
            }

            rows++;
            // 键路径中出现 [ 说明使用了数组下标（例如 list[0]、coll[0].title）
            if (trimmed.indexOf('[') > 0 && trimmed.indexOf(']') > trimmed.indexOf('[')) {
                hasIndexToken = true;
            }
        }

        return rows >= 2 || hasIndexToken;
    }

}
