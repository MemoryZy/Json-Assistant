package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.memoryzy.json.model.wrapper.ArrayWrapper;
import cn.memoryzy.json.model.wrapper.JsonWrapper;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;

import java.util.*;

/**
 * CSV 与 JSON 双向转换工具类。
 * <p>
 * 核心转换逻辑与 UI 解耦，仅依赖 JSON 包装对象（{@link JsonWrapper}）与 hutool/Jackson 等既有依赖，
 * 不依赖任何 IntelliJ UI 组件。
 * </p>
 *
 * @author Memory
 * @since 2026/09/16
 */
public class CsvUtil {

    /**
     * 判断给定文本是否为有效的 JSON 数组对象（可作为 CSV 转换源）。
     *
     * @param json json 字符串
     * @return 若为 JSON 数组且元素为对象则返回 true
     */
    public static boolean isArrayOfObjects(String json) {
        if (StrUtil.isBlank(json)) return false;
        JsonWrapper wrapper;
        if (JsonUtil.isJson(json)) {
            wrapper = JsonUtil.parse(json);
        } else if (Json5Util.isJson5(json)) {
            wrapper = Json5Util.parse(json);
        } else {
            return false;
        }

        if (wrapper == null || !wrapper.isArray()) return false;
        ArrayWrapper items = (ArrayWrapper) wrapper;
        for (Object item : items) {
            if (!(item instanceof Map)) return false;
        }
        return true;
    }


    /**
     * 将 JSON 数组对象转换为 CSV 文本。
     * <ul>
     *   <li>表头取所有对象 key 的并集（保持首次出现顺序）；</li>
     *   <li>正确转义双引号、逗号、换行；</li>
     *   <li>嵌套对象/数组序列化为 JSON 字符串后作为单个单元格写入。</li>
     * </ul>
     *
     * @param json   JSON 字符串
     * @param isJson 是否标准 JSON（false 则按 JSON5 处理）
     * @return CSV 文本，若转换失败返回 null
     */
    public static String jsonToCsv(String json, boolean isJson) {
        JsonWrapper wrapper = resolveArray(json, isJson);
        if (wrapper == null || !wrapper.isArray()) return null;

        List<Object> items = asList(wrapper);
        if (items == null) return null;

        List<ObjectWrapper> rows = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();

        for (Object item : items) {
            ObjectWrapper row = asObjectWrapper(item);
            if (row == null) {
                // 数组元素存在非对象时无法形成表格，返回 null
                return null;
            }
            rows.add(row);
            headers.addAll(row.keySet());
        }

        List<String> headerList = new ArrayList<>(headers);
        StringBuilder sb = new StringBuilder();

        // 表头行
        sb.append(joinCsvLine(headerList)).append('\n');

        // 数据行
        for (ObjectWrapper row : rows) {
            List<String> values = new ArrayList<>(headerList.size());
            for (String header : headerList) {
                values.add(cellToString(row.get(header)));
            }
            sb.append(joinCsvLine(values)).append('\n');
        }

        // 去掉最后一个多余换行符
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }


    /**
     * 判断给定文本是否可被解析为 CSV（用于反向转换的策略识别）。
     *
     * @param csv CSV 文本
     * @return 可解析为 CSV 时返回 true
     */
    public static boolean canCsvBeConvertedToJson(String csv) {
        if (StrUtil.isBlank(csv)) return false;
        // 已经是 JSON 或 JSON5 的文本不按 CSV 处理
        if (JsonUtil.canResolveToJson(csv) || Json5Util.isJson5(csv)) return false;
        return parseGrid(csv) != null;
    }


    /**
     * 将 CSV 文本转换为 JSON 数组文本（默认启用类型推断）。
     *
     * @param csv CSV 文本
     * @return 格式化后的 JSON 数组文本，若转换失败返回 null
     */
    public static String csvToJson(String csv) {
        return csvToJson(csv, true);
    }


    /**
     * 将 CSV 文本转换为 JSON 数组文本。
     * <ul>
     *   <li>支持首行表头；</li>
     *   <li>支持双引号包裹与转义（{@code ""} 表示引号）；</li>
     *   <li>类型推断可配置：{@code inferTypes} 为 true 时自动推断数字/布尔/null，否则全部按字符串处理。</li>
     * </ul>
     *
     * @param csv        CSV 文本
     * @param inferTypes 是否对单元格值进行类型推断
     * @return 格式化后的 JSON 数组文本，若转换失败返回 null
     */
    public static String csvToJson(String csv, boolean inferTypes) {
        List<List<String>> grid = parseGrid(csv);
        if (CollUtil.isEmpty(grid)) return null;

        List<String> headers = grid.get(0);
        int columnCount = headers.size();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i < grid.size(); i++) {
            List<String> row = grid.get(i);
            Map<String, Object> record = new LinkedHashMap<>();
            for (int c = 0; c < columnCount; c++) {
                String header = headers.get(c);
                String cell = c < row.size() ? row.get(c) : "";
                record.put(header, inferTypes ? JsonAssistantUtil.detectType(cell) : cell);
            }
            result.add(record);
        }

        return JsonUtil.formatJson(result);
    }


    /**
     * 解析 CSV 文本为二维字符串网格（含表头行）。
     * <p>实现标准 RFC 4180 解析：逗号分隔、双引号包裹、双引号转义、字段内换行。</p>
     *
     * @param csv CSV 文本
     * @return 二维网格，解析失败返回 null
     */
    private static List<List<String>> parseGrid(String csv) {
        List<List<String>> grid = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean hasAnyField = false;

        int len = csv.length();
        for (int i = 0; i < len; i++) {
            char c = csv.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // 双引号转义
                    if (i + 1 < len && csv.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                switch (c) {
                    case '"':
                        inQuotes = true;
                        hasAnyField = true;
                        break;
                    case ',':
                        row.add(field.toString());
                        field.setLength(0);
                        hasAnyField = true;
                        break;
                    case '\r':
                        // 兼容 \r\n 与单独 \r
                        if (i + 1 < len && csv.charAt(i + 1) == '\n') {
                            i++;
                        }
                        row.add(field.toString());
                        field.setLength(0);
                        grid.add(row);
                        row = new ArrayList<>();
                        hasAnyField = true;
                        break;
                    case '\n':
                        row.add(field.toString());
                        field.setLength(0);
                        grid.add(row);
                        row = new ArrayList<>();
                        hasAnyField = true;
                        break;
                    default:
                        field.append(c);
                        hasAnyField = true;
                        break;
                }
            }
        }

        // 处理最后一个字段与最后一行
        if (field.length() > 0 || hasAnyField) {
            row.add(field.toString());
            grid.add(row);
        }

        // 若未解析出任何有效表格则视为失败
        if (grid.isEmpty()) return null;

        // 校验：所有行是否等宽（等宽才可形成表格），若只有表头一行也允许（空表 → JSON 空数组）
        int width = grid.get(0).size();
        for (List<String> r : grid) {
            if (r.size() != width) return null;
        }

        return grid;
    }


    /**
     * 将 CSV 的一行单元格拼接为 CSV 行字符串，处理转义与换行。
     */
    private static String joinCsvLine(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsvCell(cells.get(i)));
        }
        return sb.toString();
    }


    /**
     * 转义单个 CSV 单元格：含逗号、双引号、换行的字段使用双引号包裹，双引号转义为两个双引号。
     */
    private static String escapeCsvCell(String cell) {
        if (cell == null) cell = "";
        boolean needQuote = cell.indexOf(',') >= 0
                || cell.indexOf('"') >= 0
                || cell.indexOf('\n') >= 0
                || cell.indexOf('\r') >= 0;
        if (needQuote) {
            return '"' + cell.replace("\"", "\"\"") + '"';
        }
        return cell;
    }


    /**
     * 将 JSON 值转换为 CSV 单元格字符串。
     * <p>嵌套对象/数组序列化为 JSON 字符串；null 转空字符串。</p>
     */
    private static String cellToString(Object value) {
        if (Objects.isNull(value)) return "";
        if (value instanceof Map || value instanceof List) {
            // 嵌套结构序列化为 JSON 字符串（压缩为单行）
            String json = JsonUtil.compressJson(value);
            return json == null ? String.valueOf(value) : json;
        }
        // 字符串保持原样（由 escapeCsvCell 处理转义）
        return String.valueOf(value);
    }


    /**
     * 将 JSON 文本解析为数组包装对象。
     */
    private static JsonWrapper resolveArray(String json, boolean isJson) {
        if (StrUtil.isBlank(json)) return null;
        try {
            return isJson ? JsonUtil.parse(json) : Json5Util.parse(json);
        } catch (Exception ignored) {
            return null;
        }
    }


    /**
     * 将数组元素包装为对象包装对象，非对象元素返回 null。
     */
    private static ObjectWrapper asObjectWrapper(Object item) {
        if (item instanceof ObjectWrapper) return (ObjectWrapper) item;
        if (item instanceof Map) return new ObjectWrapper(item);
        return null;
    }


    /**
     * 将 JSON 数组包装对象转换为 List，若非数组或无法转换返回 null。
     */
    private static List<Object> asList(JsonWrapper wrapper) {
        if (!(wrapper instanceof Collection)) return null;
        return new ArrayList<>((Collection<Object>) wrapper);
    }

}