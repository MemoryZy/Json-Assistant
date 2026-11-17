package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * JSON 值整理工具
 * <p>
 * 用于「整理」JSON / JSON5 文本，也就是去除其中的「无意义数据」。无意义数据一共分为三类：
 * <ol>
 *     <li><b>null 值</b>：JSON 中的 {@code null}</li>
 *     <li><b>空值</b>：空字符串（含仅由空白字符组成的字符串）、空对象 {@code {}}、空数组 {@code []}</li>
 *     <li><b>0 值</b>：数值类型的 0（含 {@code 0.0}、{@code -0} 等等价写法）</li>
 * </ol>
 * 调用方通过 {@link Mode} 指定「只去除某一类」，或者「三类一起去除」，
 * 对应插件菜单中「整理 Json…」下的各个子项（去除 null 值 / 去除空值 / 去除 0 值 / 全部去除）。
 * <p>
 * 需要特别说明的判定细节：
 * <ul>
 *     <li>布尔值 {@code false} 不被视为 0 值，会原样保留</li>
 *     <li>字符串 {@code "0"} 属于有内容的字符串，会原样保留</li>
 *     <li>数组元素同样参与清理（会改变数组长度），因此数组中的 null、空字符串、0 会被移除</li>
 *     <li>嵌套的对象/数组会先递归清理，若清理后变成空对象/空数组，则它所在的字段（或数组元素）也会被移除。
 *     注意：级联移除空容器属于「空值」的范畴，因此只在 {@link Mode#EMPTY} 与 {@link Mode#ALL} 模式下发生；
 *     例如只去除 null 值时，{@code {"a": {"b": null}}} 会整理成 {@code {"a": {}}}，而不会把 {@code a} 也删掉</li>
 *     <li>JSON5 的注释保存在特殊键（{@link cn.memoryzy.json.constant.PluginConstant#COMMENT_KEY}）中，
 *     该键不参与清理，以保证注释不丢失</li>
 * </ul>
 * 整理后的文本会保持原本的排版风格：原本是格式化文本则继续输出格式化文本，原本是压缩文本则继续输出压缩文本。
 *
 * @author Memory
 * @since 2026/11/17
 */
public class JsonValuePruner {

    /**
     * 整理模式（决定「去除哪几类无意义数据」）
     */
    public enum Mode {

        /**
         * 只去除 null 值
         */
        NULL,

        /**
         * 只去除空值：空字符串、空对象、空数组（含递归清理后变空的容器）
         */
        EMPTY,

        /**
         * 只去除 0 值（数值类型的 0）
         */
        ZERO,

        /**
         * 去除全部：null 值 + 空值 + 0 值
         */
        ALL;
    }

    /**
     * 累计被移除的节点数量（字段数 + 数组元素数，含级联移除的空容器）
     */
    private int removedCount;

    private JsonValuePruner() {
    }

    /**
     * 去除 JSON / JSON5 文本中的全部无意义数据（等价于 {@code prune(json, Mode.ALL)}）
     *
     * @param json JSON 或 JSON5 文本
     * @return 清理结果；若文本为空或无法解析，则返回 {@code null}
     */
    public static PruneResult prune(String json) {
        return prune(json, Mode.ALL);
    }

    /**
     * 按指定模式去除 JSON / JSON5 文本中的无意义数据
     *
     * @param json JSON 或 JSON5 文本
     * @param mode 整理模式，传 {@code null} 时按 {@link Mode#ALL} 处理
     * @return 清理结果；若文本为空或无法解析，则返回 {@code null}
     */
    public static PruneResult prune(String json, Mode mode) {
        if (StrUtil.isBlank(json)) {
            return null;
        }

        // 没有指定模式时，默认「全部去除」，避免调用方漏传参数导致什么都不做
        Mode pruneMode = mode == null ? Mode.ALL : mode;

        // 优先按标准 JSON 解析，若不是 JSON 则按 JSON5 解析（JSON5 需要保留注释）
        boolean isJson = JsonUtil.isJson(json);
        JsonWrapper wrapper = isJson ? JsonUtil.parse(json) : Json5Util.parseWithComment(json);
        if (wrapper == null) {
            return null;
        }

        JsonValuePruner pruner = new JsonValuePruner();
        // 根节点同样是对象或数组，直接复用同一套递归清理逻辑
        JsonWrapper prunedWrapper = (JsonWrapper) pruner.pruneAny(wrapper, pruneMode);

        // 依照原文本的排版风格输出，避免整理操作额外改变文本格式
        String prunedText;
        if (JsonUtil.isFormattedJson(json)) {
            prunedText = isJson ? JsonUtil.formatJson(prunedWrapper) : Json5Util.formatJson5(prunedWrapper);
        } else {
            prunedText = isJson ? JsonUtil.compressJson(prunedWrapper) : Json5Util.compressJson5(prunedWrapper);
        }

        if (StrUtil.isBlank(prunedText)) {
            return null;
        }

        return new PruneResult(prunedText, pruner.removedCount);
    }

    /**
     * 递归清理任意节点
     * <p>
     * 标量（字符串、数字、布尔）内部没有可清理的内容，因此原样返回；
     * 是否丢弃该标量，由它所在的父容器（{@link #pruneObject} / {@link #pruneArray}）来决定。
     *
     * @param value 待清理的节点
     * @param mode  整理模式
     * @return 清理后的节点
     */
    private Object pruneAny(Object value, Mode mode) {
        if (value instanceof ObjectWrapper) {
            return pruneObject((ObjectWrapper) value, mode);
        }

        if (value instanceof ArrayWrapper) {
            return pruneArray((ArrayWrapper) value, mode);
        }

        return value;
    }

    /**
     * 清理对象类型节点：逐个字段判断「值」是否需要被移除
     *
     * @param source 原对象
     * @param mode   整理模式
     * @return 清理后的对象（新对象，不修改原对象）
     */
    private ObjectWrapper pruneObject(ObjectWrapper source, Mode mode) {
        ObjectWrapper target = new ObjectWrapper();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // JSON5 的注释以特殊键的形式保存在对象中，必须原样保留，否则注释会丢失
            if (Json5Util.isCommentKey(key, value)) {
                target.put(key, value);
                continue;
            }

            // 先递归处理嵌套的对象/数组，再判断清理后的值是否需要被丢弃
            Object prunedValue = pruneAny(value, mode);
            if (shouldRemove(prunedValue, mode)) {
                removedCount++;
                continue;
            }

            target.put(key, prunedValue);
        }

        return target;
    }

    /**
     * 清理数组类型节点
     * <p>
     * 注意：数组元素被移除后，数组长度与元素下标都会发生变化，这是该功能预期的行为。
     *
     * @param source 原数组
     * @param mode   整理模式
     * @return 清理后的数组（新数组，不修改原数组）
     */
    private ArrayWrapper pruneArray(ArrayWrapper source, Mode mode) {
        ArrayWrapper target = new ArrayWrapper();

        for (Object element : source) {
            // 先递归处理嵌套的对象/数组，再判断清理后的元素是否需要被丢弃
            Object prunedElement = pruneAny(element, mode);
            if (shouldRemove(prunedElement, mode)) {
                removedCount++;
                continue;
            }

            target.add(prunedElement);
        }

        return target;
    }

    /**
     * 判断某个节点的值是否需要被移除（null 值 / 空值 / 0 值，具体看模式）
     *
     * @param value 节点的值（已经过递归清理）
     * @param mode  整理模式
     * @return 需要移除为 true，需要保留为 false
     */
    private static boolean shouldRemove(Object value, Mode mode) {
        // 1、null 值
        if (value == null) {
            return matches(mode, Mode.NULL);
        }

        // 2、空值：空字符串、仅由空白字符组成的字符串
        if (value instanceof CharSequence) {
            return matches(mode, Mode.EMPTY) && StrUtil.isBlank(value.toString());
        }

        // 3、0 值：数值类型的 0（0.0、-0 等写法同样视为 0）
        if (value instanceof Number) {
            return matches(mode, Mode.ZERO) && isZeroValue((Number) value);
        }

        // 4、空值：空对象、空数组（包含「递归清理后变空」的情况）
        if (value instanceof JsonWrapper) {
            return matches(mode, Mode.EMPTY) && isEmptyContainer((JsonWrapper) value);
        }

        // 其余类型（如布尔值 true/false）保留
        return false;
    }

    /**
     * 判断当前模式是否包含目标类别
     * <p>
     * {@link Mode#ALL} 等价于「三类都包含」，因此只要有它就直接命中。
     *
     * @param mode   当前模式
     * @param target 目标类别
     * @return 包含则为 true
     */
    private static boolean matches(Mode mode, Mode target) {
        return mode == Mode.ALL || mode == target;
    }

    /**
     * 判断数值是否为 0
     * <p>
     * 针对不同类型分别判断，避免把高精度小数（如 1e-400）误判成 0。
     *
     * @param number 数值
     * @return 为 0 则返回 true
     */
    private static boolean isZeroValue(Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).compareTo(BigDecimal.ZERO) == 0;
        }

        if (number instanceof BigInteger) {
            return ((BigInteger) number).signum() == 0;
        }

        if (number instanceof Double || number instanceof Float) {
            // 使用 double 比较，可同时覆盖 -0.0 这种写法
            return number.doubleValue() == 0.0d;
        }

        return number.longValue() == 0L;
    }

    /**
     * 判断容器（对象/数组）是否为空
     * <p>
     * 对象中可能同时存在 JSON5 的注释键，注释键不计入「有效数据」，因此需要单独判断。
     *
     * @param wrapper 容器
     * @return 没有有效数据则为 true
     */
    private static boolean isEmptyContainer(JsonWrapper wrapper) {
        if (wrapper instanceof ObjectWrapper) {
            for (Map.Entry<String, Object> entry : ((ObjectWrapper) wrapper).entrySet()) {
                if (!Json5Util.isCommentKey(entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }

        return wrapper.noItems();
    }

    /**
     * 清理结果
     */
    public static class PruneResult {

        /**
         * 清理后的文本
         */
        private final String text;

        /**
         * 被移除的节点数量
         */
        private final int removedCount;

        public PruneResult(String text, int removedCount) {
            this.text = text;
            this.removedCount = removedCount;
        }

        public String getText() {
            return text;
        }

        public int getRemovedCount() {
            return removedCount;
        }

        /**
         * 是否确实移除了数据
         * <p>
         * 用于「没有可去除的值」时给出提示，而不是无意义地重写一遍文本。
         *
         * @return 移除过数据则为 true
         */
        public boolean isChanged() {
            return removedCount > 0;
        }
    }
}
