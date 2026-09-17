package cn.memoryzy.json.model.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 完整的排序策略实现（包含自然排序）
 *
 * @author Memory
 * @since 2025/9/1
 */
public class SortStrategies {

    /**
     * 随机打乱顺序
     */
    public static final SortStrategy SHUFFLE = pairs -> {
        Collections.shuffle(pairs);
        return pairs;
    };

    /**
     * 完全反转当前顺序
     */
    public static final SortStrategy REVERSE = pairs -> {
        List<KeyValuePair> reversed = new ArrayList<>(pairs);
        Collections.reverse(reversed);
        return reversed;
    };

    /**
     * 区分大小写的正序（A→Z）
     */
    public static final SortStrategy CASE_SENSITIVE_A_Z = pairs ->
            sortByComparator(pairs, Comparator.comparing(KeyValuePair::getKey));

    /**
     * 区分大小写的倒序（Z→A）
     */
    public static final SortStrategy CASE_SENSITIVE_Z_A = pairs ->
            sortByComparator(pairs, Comparator.comparing(KeyValuePair::getKey).reversed());

    /**
     * 不区分大小写的正序（A→Z）
     */
    public static final SortStrategy CASE_INSENSITIVE_A_Z = pairs ->
            sortByComparator(pairs, Comparator.comparing(p -> p.getKey().toLowerCase()));

    /**
     * 不区分大小写的倒序（Z→A）
     */
    public static final SortStrategy CASE_INSENSITIVE_Z_A = pairs ->
            sortByComparator(pairs, Comparator.comparing((KeyValuePair p) -> p.getKey().toLowerCase()).reversed());

    /**
     * 按字符串长度升序
     */
    public static final SortStrategy LINE_LENGTH_SHORT_LONG = pairs ->
            sortByComparator(pairs, Comparator.comparingInt(p -> p.getKey().length()));

    /**
     * 按字符串长度降序
     */
    public static final SortStrategy LINE_LENGTH_LONG_SHORT = pairs ->
            sortByComparator(pairs, Comparator.comparingInt((KeyValuePair p) -> p.getKey().length()).reversed());

    /**
     * 按十六进制数值排序
     */
    public static final SortStrategy HEXA = pairs -> sortByComparator(pairs, new HexComparator());

    /**
     * 自然排序正序
     */
    public static final SortStrategy NATURAL_ORDER_A_Z = pairs ->
            sortByComparator(pairs, new NaturalOrderComparator());

    /**
     * 自然排序倒序
     */
    public static final SortStrategy NATURAL_ORDER_Z_A = pairs ->
            sortByComparator(pairs, new NaturalOrderComparator().reversed());

    private static List<KeyValuePair> sortByComparator(List<KeyValuePair> pairs, Comparator<KeyValuePair> comparator) {
        return pairs.stream().sorted(comparator).collect(Collectors.toList());
    }
}