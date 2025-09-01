package cn.memoryzy.json.model.sort;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * 自然排序比较器
 *
 * @author Memory
 * @since 2025/9/1
 */
public class NaturalOrderComparator implements Comparator<KeyValuePair> {
    @Override
    public int compare(KeyValuePair p1, KeyValuePair p2) {
        return compareStrings(p1.getKey(), p2.getKey());
    }

    public @NotNull NaturalOrderComparator reversed() {
        return new NaturalOrderComparator() {
            @Override
            public int compare(KeyValuePair p1, KeyValuePair p2) {
                return -NaturalOrderComparator.this.compareStrings(p1.getKey(), p2.getKey());
            }
        };
    }

    private int compareStrings(String a, String b) {
        int ia = 0, ib = 0;
        int nza = 0, nzb = 0;
        char ca, cb;

        while (true) {
            // 跳过前导零
            nza = nzb = 0;
            ca = charAt(a, ia);
            cb = charAt(b, ib);

            while (ca == '0') {
                nza++;
                ca = charAt(a, ++ia);
            }

            while (cb == '0') {
                nzb++;
                cb = charAt(b, ++ib);
            }

            // 处理数字块
            if (isDigit(ca) && isDigit(cb)) {
                int bias = compareRight(a.substring(ia), b.substring(ib));
                if (bias != 0) return bias;
            }

            if (ca == 0 && cb == 0) {
                return compareEqual(a, b, nza, nzb);
            }

            if (ca < cb) return -1;
            if (ca > cb) return 1;

            ia++;
            ib++;
        }
    }

    private int compareRight(String a, String b) {
        int bias = 0, ia = 0, ib = 0;

        for (; ; ia++, ib++) {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);

            if (!isDigit(ca) && !isDigit(cb)) return bias;
            if (!isDigit(ca)) return -1;
            if (!isDigit(cb)) return +1;
            if (ca == 0 && cb == 0) return bias;

            if (bias == 0) {
                if (ca < cb) bias = -1;
                else if (ca > cb) bias = +1;
            }
        }
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c) || c == '.' || c == ',';
    }

    private char charAt(String s, int i) {
        return i >= s.length() ? 0 : s.charAt(i);
    }

    private int compareEqual(String a, String b, int nza, int nzb) {
        if (nza - nzb != 0) return nza - nzb;
        if (a.length() == b.length()) return a.compareTo(b);
        return a.length() - b.length();
    }
}