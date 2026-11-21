package cn.memoryzy.json.util;

import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.model.wrapper.JsonWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Bulk KV Lines（批量键值行格式）与 JSON 的双向转换工具类。
 * <p>
 * Bulk KV Lines（批量键值行格式）的规则：
 * <ol>
 *     <li>一行一个键值对，行与行之间用换行分隔；</li>
 *     <li>键与值之间用第一个英文冒号 {@code :} 分隔，值里可以继续出现冒号（例如 URL）；</li>
 *     <li>键用「点号 + 方括号下标」表示层级：{@code metadata.author}、{@code list[0]}、
 *     {@code coll[0].actions[1].label}，因此嵌套对象、嵌套数组对象都可以表达；</li>
 *     <li>在行首加 {@code //} 表示该行保留但暂时禁用；</li>
 *     <li>值按字面量推断类型：{@code true} / {@code false} 为布尔，{@code null} 为空值，
 *     纯数字为数字，被双引号包裹的按字符串处理，其余都按字符串处理，冒号后什么都不写即空字符串；
 *     位数超过 {@value #MAX_PLAIN_DIGITS} 的数值也按字符串处理（避免超长数字在 JSON 中丢失精度）。
 *     该推断可通过 {@link #bulkToJson(String, boolean)} 关闭，关闭后所有值都按字符串处理。</li>
 * </ol>
 * 该类只做纯文本与 JSON 之间的转换，不依赖任何 IntelliJ 平台类，便于单独验证与复用。
 * <p>
 * 两个方向的转换策略：
 * <ul>
 *     <li><b>JSON → Bulk KV Lines</b>：按 JSON 的原始顺序深度优先遍历，对象成员拼成 {@code a.b}，
 *     数组元素拼成 {@code a[0]}，叶子节点输出为一行；空对象 / 空数组在该格式中没有对应写法，
 *     会被跳过（该限制由格式本身决定，不是本实现的取舍）。</li>
 *     <li><b>Bulk KV Lines → JSON</b>：逐行解析键路径与值，按路径重建 {@code LinkedHashMap} / {@code ArrayList}，
 *     保持行序；数组下标可以不连续（缺失的位置补 {@code null}），
 *     与常见实现一致；以 {@code //} 开头的禁用行会被跳过。</li>
 * </ul>
 *
 * @author Memory
 * @since 2026/11/19
 */
public class BulkKvLinesUtil {

    /**
     * 禁用行的前缀：该格式中「保留但禁用」的行
     */
    private static final String DISABLED_ROW_PREFIX = "//";

    /**
     * 数字字面量：用于「值类型推断」与「字符串是否需要在输出时加引号」两处判断
     * <p>
     * 覆盖整数、小数与科学计数法（例如 {@code 0}、{@code -0.0}、{@code 1e-400}），
     * 与 JSON 数字的写法保持一致。
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

    /**
     * 根为数组时的首行形态：{@code [下标]} 之后紧跟 {@code .}、{@code [} 或 {@code :}，
     * 例如 {@code [0].a:1}、{@code [0][0]:1}、{@code [0]:1}；用于把它与 JSON 数组区分开。
     */
    private static final Pattern ROOT_INDEX_ROW = Pattern.compile("^\\[[0-9]+\\](?:[.\\[:])");

    /**
     * 数字按“字符串”处理的最大位数
     * <p>
     * 超过该位数的数值（例如 19 位的雪花 ID、超长订单号）默认按字符串处理：
     * 这类数字在 JavaScript 等消费端会超出安全整数范围（2^53 - 1 只有 16 位），
     * 用数字类型承载会静默丢失精度，而它们的语义通常本身就是标识而非可运算的量。
     */
    private static final int MAX_PLAIN_DIGITS = 15;

    /**
     * 私有用构造方法，工具类不允许实例化
     */
    private BulkKvLinesUtil() {
    }


    // ================================ JSON -> Bulk ================================ //

    /**
     * 将 JSON（或 JSON5）文本转换为 Bulk KV Lines 文本。
     *
     * @param json   JSON 文本
     * @param isJson 是否标准 JSON（false 则按 JSON5 解析）
     * @return Bulk 文本；解析失败或没有任何可输出的叶子节点时返回 null
     */
    public static String jsonToBulk(String json, boolean isJson) {
        if (StrUtil.isBlank(json)) {
            return null;
        }

        JsonWrapper wrapper = isJson ? JsonUtil.parse(json) : Json5Util.parse(json);
        if (Objects.isNull(wrapper)) {
            return null;
        }

        List<String> rows = new ArrayList<>();
        appendRows(wrapper, "", rows);
        return rows.isEmpty() ? null : String.join("\n", rows);
    }

    /**
     * 递归收集 Bulk 行
     *
     * @param value 当前节点（对象 / 数组 / 标量）
     * @param path  当前节点对应的键路径（根节点为空串）
     * @param rows  收集结果
     */
    private static void appendRows(Object value, String path, List<String> rows) {
        if (value instanceof Map) {
            // 对象：成员拼成「父路径.键名」，根节点的成员直接使用键名
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                String key = String.valueOf(entry.getKey());
                // JSON5 的注释会以特殊键保存，不能当成真实字段输出
                if (PluginConstant.COMMENT_KEY.equals(key)) {
                    continue;
                }

                String childPath = path.isEmpty() ? key : path + "." + key;
                appendRows(entry.getValue(), childPath, rows);
            }
            return;
        }

        if (value instanceof Collection) {
            // 数组：元素拼成「父路径[下标]」，下标从 0 开始
            int index = 0;
            for (Object item : (Collection<?>) value) {
                appendRows(item, path + "[" + index + "]", rows);
                index++;
            }
            return;
        }

        // 标量：只有拿到键路径才能输出成一行；根节点本身是标量时无法表达，直接忽略
        if (!path.isEmpty()) {
            rows.add(path + ":" + formatValue(value));
        }
    }

    /**
     * 将标量值格式化为 Bulk 的「值」部分
     *
     * @param value 标量值
     * @return Bulk 中的值文本
     */
    private static String formatValue(Object value) {
        if (Objects.isNull(value)) {
            return "null";
        }

        if (value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Number) {
            return formatNumber((Number) value);
        }

        return quoteIfNeeded(String.valueOf(value));
    }

    /**
     * 格式化数字，避免 {@link BigDecimal} 输出科学计数法（例如 {@code 1E+2}）
     *
     * @param number 数字
     * @return 数字文本
     */
    private static String formatNumber(Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).toPlainString();
        }

        return number.toString();
    }

    /**
     * 按需给字符串值加双引号
     * <p>
     * 加引号的目的只有一个：让「值」在回读时仍然是字符串，而不是被推断成数字 / 布尔 / null。
     * 例如 JSON 中的字符串 {@code "0"}、{@code "true"}、{@code "null"}，
     * 若不加引号写入 Bulk KV Lines，回读时会分别被推断成数字 0、布尔 true 与 null，类型就丢了。
     * 另外，含换行 / 引号 / 反斜杠 / 首尾空格的字符串也需要靠引号与转义才能安全表达。
     *
     * @param text 字符串值
     * @return 可直接写入 Bulk 的值文本
     */
    private static String quoteIfNeeded(String text) {
        // 空字符串直接写成「键:」，回读时同样是空字符串，无需加引号
        if (text.isEmpty()) {
            return text;
        }

        return needsQuote(text) ? quote(text) : text;
    }

    /**
     * 判断字符串是否需要加双引号
     *
     * @param text 字符串值
     * @return 需要加引号返回 true
     */
    private static boolean needsQuote(String text) {
        // 会被推断成数字 / 布尔 / null 的文本，必须加引号才能保住字符串类型
        if (NUMBER_PATTERN.matcher(text).matches()
                || "true".equalsIgnoreCase(text)
                || "false".equalsIgnoreCase(text)
                || "null".equalsIgnoreCase(text)) {
            return true;
        }

        // 首尾空格在解析时会被裁掉，需要加引号保留
        if (text.startsWith(" ") || text.endsWith(" ")) {
            return true;
        }

        // 换行、制表符、引号、反斜杠都需要转义后才能写在一行里
        for (char c : text.toCharArray()) {
            if (c == '"' || c == '\\' || c == '\n' || c == '\r' || c == '\t') {
                return true;
            }
        }

        return false;
    }

    /**
     * 给字符串值加双引号，并转义其中的特殊字符
     *
     * @param text 字符串值
     * @return 加引号后的值文本
     */
    private static String quote(String text) {
        StringBuilder builder = new StringBuilder(text.length() + 2).append('"');
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(c);
            }
        }

        return builder.append('"').toString();
    }


    // ================================ Bulk -> JSON ================================ //

    /**
     * 将 Bulk KV Lines 文本转换为格式化的 JSON 文本（对值做类型推断）。
     *
     * @param bulk Bulk 文本
     * @return 格式化后的 JSON 文本；没有有效行或解析失败时返回 null
     */
    public static String bulkToJson(String bulk) {
        return bulkToJson(bulk, true);
    }

    /**
     * 将 Bulk KV Lines 文本转换为格式化的 JSON 文本，可选择是否对值做类型推断。
     * <p>
     * 两个入口共用同一套解析逻辑，只有「值」的处理不同：
     * <ul>
     *     <li>{@code inferTypes = true}：按字面量推断 {@code null} / 布尔 / 数字；</li>
     *     <li>{@code inferTypes = false}：所有值都保持字符串，{@code null}、{@code true}、{@code 1}
     *     这类文本原样写入，不做任何类型转换。被双引号包裹的值在两种模式下都会去掉引号并反转义
     *     （引号只表示「这一定是字符串」）。</li>
     * </ul>
     *
     * @param bulk       Bulk 文本
     * @param inferTypes 是否对值做类型推断
     * @return 格式化后的 JSON 文本；没有有效行或解析失败时返回 null
     */
    public static String bulkToJson(String bulk, boolean inferTypes) {
        if (StrUtil.isBlank(bulk)) {
            return null;
        }

        List<Row> rows = parseRows(bulk, inferTypes);
        if (rows.isEmpty()) {
            return null;
        }

        // 根节点的类型由第一行的首个路径片段决定：以 [0] 开头说明根是数组，否则是对象
        Object root = rows.get(0).tokens.get(0).indexToken
                ? new ArrayList<>()
                : new LinkedHashMap<String, Object>();

        for (Row row : rows) {
            assign(root, row.tokens, row.value);
        }

        return JsonUtil.formatJson(root);
    }

    /**
     * 判断文本是否可以被当作 Bulk KV Lines 文本转换为 JSON
     * <p>
     * 判断口径：首行不是 JSON / JSON5 的结构（{@code {} 开头的对象、非「下标行」的 {@code [} 开头的数组）；
     * 每一行（忽略空行与 {@code //} 禁用行）都能被解析成 {@code 键:值} 形式的合法键路径。
     * <p>
     * 这里没有用 JSON / JSON5 的解析器做前置判断：项目的 JSON 解析比较宽松，
     * {@code [0].a:1} 这类「根为数组的 Bulk」也会被判成 JSON，用解析器判断会把合法的 Bulk 挡掉。
     *
     * @param text 待判断文本
     * @return 可以转换返回 true
     */
    public static boolean canBulkBeConvertedToJson(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        boolean hasRow = false;
        for (String line : text.split("\\r?\\n")) {
            String trimmed = StrUtil.trim(line);
            // 空行与禁用行不参与判断
            if (trimmed.isEmpty() || trimmed.startsWith(DISABLED_ROW_PREFIX)) {
                continue;
            }

            // 只对第一行做结构判断：JSON / JSON5 的文本整体形态与 Bulk 不同
            if (!hasRow && looksLikeJsonStructure(trimmed)) {
                return false;
            }

            // Bulk 的每一行必须是「键:值」，没有冒号的普通文本不算 Bulk
            if (trimmed.indexOf(':') <= 0 || parsePath(splitKey(trimmed)) == null) {
                return false;
            }

            hasRow = true;
        }

        return hasRow;
    }

    /**
     * 判断首行是否像 JSON / JSON5 的结构，而不是 Bulk 的键值行
     *
     * @param firstLine 第一行有效内容（已去除首尾空白）
     * @return 像 JSON 结构返回 true
     */
    private static boolean looksLikeJsonStructure(String firstLine) {
        if (firstLine.startsWith("{")) {
            return true;
        }

        if (firstLine.startsWith("[")) {
            // 根为数组的 Bulk 以「[下标]」开头，例如 [0].a:1、[0][0]:1；
            // 其余以 [ 开头的内容按 JSON / JSON5 数组处理
            return !ROOT_INDEX_ROW.matcher(firstLine).find();
        }

        return false;
    }

    /**
     * 逐行解析 Bulk 文本，得到「键路径 + 值」的列表
     *
     * @param bulk       Bulk 文本
     * @param inferTypes 是否对值做类型推断（false 时所有值按字符串处理）
     * @return 解析结果（不含空行与禁用行）
     */
    private static List<Row> parseRows(String bulk, boolean inferTypes) {
        List<Row> rows = new ArrayList<>();
        for (String line : bulk.split("\\r?\\n")) {
            String trimmed = StrUtil.trim(line);
            // 空行忽略
            if (trimmed.isEmpty()) {
                continue;
            }

            // 以 // 开头的行在该格式中表示「保留但禁用」，这里跳过，不写入 JSON
            if (trimmed.startsWith(DISABLED_ROW_PREFIX)) {
                continue;
            }

            // 键与值用第一个冒号分隔，值中允许出现冒号（例如 https://example.com）
            int colonIndex = trimmed.indexOf(':');
            // 没有冒号的行无法构成键值对，直接跳过
            if (colonIndex <= 0) {
                continue;
            }

            String keyPart = trimmed.substring(0, colonIndex);
            String valuePart = trimmed.substring(colonIndex + 1);

            List<KeyToken> tokens = parsePath(keyPart);
            // 键路径非法时跳过该行，避免整段转换失败
            if (tokens == null) {
                continue;
            }

            rows.add(new Row(tokens, parseValue(StrUtil.trim(valuePart), inferTypes)));
        }

        return rows;
    }

    /**
     * 取出整行中的键部分
     *
     * @param line 已去除首尾空白的行
     * @return 键部分
     */
    private static String splitKey(String line) {
        int colonIndex = line.indexOf(':');
        return StrUtil.trim(colonIndex >= 0 ? line.substring(0, colonIndex) : line);
    }

    /**
     * 解析键路径，例如 {@code coll[0].actions[1].label}
     * <p>
     * 路径由若干片段组成：名称片段（对象成员）与下标片段（数组元素）。
     * 名称片段可以包含除 {@code .}、{@code [}、{@code ]} 以外的任意字符（例如 URL 参数名里的短横线）。
     *
     * @param key 键文本
     * @return 路径片段列表；非法路径返回 null
     */
    private static List<KeyToken> parsePath(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }

        List<KeyToken> tokens = new ArrayList<>();
        int length = key.length();
        int index = 0;

        // 首段名称（根为数组时允许为空，例如 [0].name）
        StringBuilder name = new StringBuilder();
        while (index < length && key.charAt(index) != '.' && key.charAt(index) != '[' && key.charAt(index) != ']') {
            name.append(key.charAt(index++));
        }
        if (name.length() > 0) {
            tokens.add(KeyToken.ofName(name.toString()));
        }

        while (index < length) {
            char current = key.charAt(index);
            if (current == '.') {
                // .name
                index++;
                StringBuilder child = new StringBuilder();
                while (index < length && key.charAt(index) != '.' && key.charAt(index) != '[' && key.charAt(index) != ']') {
                    child.append(key.charAt(index++));
                }
                if (child.length() == 0) {
                    return null;
                }
                tokens.add(KeyToken.ofName(child.toString()));
            } else if (current == '[') {
                // [下标]
                index++;
                StringBuilder digits = new StringBuilder();
                while (index < length && Character.isDigit(key.charAt(index))) {
                    digits.append(key.charAt(index++));
                }
                if (digits.length() == 0 || index >= length || key.charAt(index) != ']') {
                    return null;
                }
                // 跳过右方括号
                index++;
                tokens.add(KeyToken.ofIndex(Integer.parseInt(digits.toString())));
            } else {
                // 出现 ] 或其他无法识别的字符，视为非法路径
                return null;
            }
        }

        return tokens.isEmpty() ? null : tokens;
    }

    /**
     * 解析 Bulk KV Lines 中的「值」
     * <p>
     * 类型推断开启时按该格式的规则推断 null / 布尔 / 数字 / 字符串；
     * 关闭时所有值都按字符串处理（双引号包裹的值仍然会去掉引号并反转义）。
     *
     * @param raw        已去除首尾空白的值文本
     * @param inferTypes 是否对值做类型推断
     * @return 解析后的值（推断时为 null / Boolean / BigDecimal / String，不推断时一定是 String）
     */
    private static Object parseValue(String raw, boolean inferTypes) {
        // 冒号后为空，表示空字符串
        if (raw.isEmpty()) {
            return "";
        }

        // 双引号包裹的值按字符串处理，需要反转义；这一步在两种模式下都要做
        boolean quoted = raw.length() >= 2 && raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"';
        String quotedContent = quoted ? unescape(raw.substring(1, raw.length() - 1)) : null;

        // 不推断类型：所有值都原样保留为字符串，null / true / false / 数字都不做转换
        if (!inferTypes) {
            return quoted ? quotedContent : raw;
        }

        if ("null".equalsIgnoreCase(raw)) {
            return null;
        }

        if ("true".equalsIgnoreCase(raw)) {
            return Boolean.TRUE;
        }

        if ("false".equalsIgnoreCase(raw)) {
            return Boolean.FALSE;
        }

        if (NUMBER_PATTERN.matcher(raw).matches()) {
            try {
                // 使用 BigDecimal 保留精度与原始量级（例如 1e-400 不会被压成 0）
                BigDecimal decimal = new BigDecimal(raw);
                // 超长数字默认按字符串处理：这类值多是 ID / 单号，用数字承载会在 JSON 消费端丢精度
                if (isTooLongNumber(decimal)) {
                    return raw;
                }
                return decimal;
            } catch (NumberFormatException ignored) {
                // 数字格式异常时退化为字符串
            }
        }

        return quoted ? quotedContent : raw;
    }

    /**
     * 判断一个数字是否“过长”，过长时应当按字符串处理
     * <p>
     * 位数按普通写法（非科学计数法）统计：整数部分与小数部分的数字个数之和，
     * 跳过符号、小数点以及整数 / 小数开头的前导 0，并忽略小数末尾无意义的 0。
     * 例如 {@code 1234567890123456}（16 位）与 {@code 1e400}（普通写法有 401 位）都算过长，
     * 而 {@code 1e-400}、{@code 0.5} 这类有效位数很少的数字仍按数字处理。
     *
     * @param decimal 已解析出的数字
     * @return 位数超过 {@link #MAX_PLAIN_DIGITS} 时返回 true
     */
    private static boolean isTooLongNumber(BigDecimal decimal) {
        // 去掉小数末尾的 0 后再取普通写法，避免 1.500 被算成 4 位
        String plain = decimal.abs().stripTrailingZeros().toPlainString();

        int digitCount = 0;
        // 是否仍处于“前导 0”阶段（0.000123 中的那些 0 不算有效位）
        boolean leadingZero = true;
        for (char c : plain.toCharArray()) {
            if (c < '0' || c > '9') {
                // 跳过小数点
                continue;
            }
            if (leadingZero && c == '0') {
                continue;
            }
            leadingZero = false;
            digitCount++;
        }

        return digitCount > MAX_PLAIN_DIGITS;
    }

    /**
     * 反转义引号内的转义字符
     *
     * @param text 引号内的原始文本
     * @return 反转义后的字符串
     */
    private static String unescape(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            char current = text.charAt(index);
            if (current != '\\' || index + 1 >= text.length()) {
                builder.append(current);
                index++;
                continue;
            }

            char next = text.charAt(index + 1);
            switch (next) {
                case 'n':
                    builder.append('\n');
                    index += 2;
                    break;
                case 'r':
                    builder.append('\r');
                    index += 2;
                    break;
                case 't':
                    builder.append('\t');
                    index += 2;
                    break;
                case '"':
                case '\\':
                    builder.append(next);
                    index += 2;
                    break;
                case 'u':
                    // Unicode 转义：反斜杠 + u + 四位十六进制数字
                    if (index + 6 <= text.length()) {
                        String hex = text.substring(index + 2, index + 6);
                        try {
                            builder.append((char) Integer.parseInt(hex, 16));
                            index += 6;
                            break;
                        } catch (NumberFormatException ignored) {
                            // 非法转义按普通字符处理
                        }
                    }
                    builder.append(current);
                    index++;
                    break;
                default:
                    builder.append(current);
                    index++;
            }
        }

        return builder.toString();
    }

    /**
     * 把一行的键路径与值写入 JSON 结构
     * <p>
     * 中途遇到类型冲突时（例如对象路径里出现数组下标）会丢弃该行，保证已有结构不被破坏。
     *
     * @param root   根容器（{@code LinkedHashMap} 或 {@code ArrayList}）
     * @param tokens 键路径片段
     * @param value  值
     */
    @SuppressWarnings("unchecked")
    private static void assign(Object root, List<KeyToken> tokens, Object value) {
        Object container = root;
        for (int index = 0; index < tokens.size(); index++) {
            KeyToken token = tokens.get(index);
            boolean last = index == tokens.size() - 1;

            if (container instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) container;
                // 对象里不可能出现数组下标，属于路径冲突
                if (token.indexToken) {
                    return;
                }

                if (last) {
                    // 同一键重复出现时以最后一行为准，与常见实现一致
                    map.put(token.name, value);
                    return;
                }

                Object child = map.get(token.name);
                if (!(child instanceof Map) && !(child instanceof Collection)) {
                    // 子节点缺失或类型不符时，按下一个片段决定新建对象还是数组
                    child = tokens.get(index + 1).indexToken ? new ArrayList<>() : new LinkedHashMap<String, Object>();
                    map.put(token.name, child);
                }
                container = child;
                continue;
            }

            List<Object> list = (List<Object>) container;
            // 数组里必须使用下标
            if (!token.indexToken) {
                return;
            }

            int arrayIndex = token.index;
            // 下标不连续时补 null 占位（与常见实现一致）
            while (list.size() <= arrayIndex) {
                list.add(null);
            }

            if (last) {
                list.set(arrayIndex, value);
                return;
            }

            Object child = list.get(arrayIndex);
            if (!(child instanceof Map) && !(child instanceof Collection)) {
                child = tokens.get(index + 1).indexToken ? new ArrayList<>() : new LinkedHashMap<String, Object>();
                list.set(arrayIndex, child);
            }
            container = child;
        }
    }


    // ================================ 内部数据结构 ================================ //

    /**
     * 键路径片段：要么是对象成员名，要么是数组下标
     */
    private static final class KeyToken {

        /**
         * 是否数组下标片段
         */
        private final boolean indexToken;

        /**
         * 对象成员名（下标片段时为空串）
         */
        private final String name;

        /**
         * 数组下标（名称片段时为 -1）
         */
        private final int index;

        private KeyToken(boolean indexToken, String name, int index) {
            this.indexToken = indexToken;
            this.name = name;
            this.index = index;
        }

        private static KeyToken ofName(String name) {
            return new KeyToken(false, name, -1);
        }

        private static KeyToken ofIndex(int index) {
            return new KeyToken(true, "", index);
        }
    }

    /**
     * 解析后的一行：键路径 + 值
     */
    private static final class Row {

        private final List<KeyToken> tokens;

        private final Object value;

        private Row(List<KeyToken> tokens, Object value) {
            this.tokens = tokens;
            this.value = value;
        }
    }
}
