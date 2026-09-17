package cn.memoryzy.json.model.sort;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * 十六进制比较器
 *
 * @author Memory
 * @since 2025/9/1
 */
public class HexComparator implements Comparator<KeyValuePair> {

    @Override
    public int compare(KeyValuePair p1, KeyValuePair p2) {
        try {
            BigInteger n1 = parseHex(p1.getKey());
            BigInteger n2 = parseHex(p2.getKey());
            return n1.compareTo(n2);
        } catch (NumberFormatException e) {
            return p1.getKey().compareTo(p2.getKey());
        }
    }

    private BigInteger parseHex(String s) {
        String clean = s.replaceFirst("^0x", "").replaceAll("[,;\\s]", "");
        return new BigInteger(clean, 16);
    }

}
