package cn.memoryzy.json.util;

import cn.memoryzy.json.model.Version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparator {

    /**
     * 判断新版本号是否高于旧版本号
     *
     * @param existingVersion 旧版本
     * @param newVersion      新版本号
     * @return 若新版本号高于旧版本号，则为 true；反之为 false
     */
    public static boolean isNewerVersion(String existingVersion, String newVersion) {
        // 条件表达式：newVersion > existingVersion
        return checkVersionConstraint(newVersion, ">" + existingVersion);
    }

    /**
     * 检查当前版本是否满足约束条件
     *
     * @param currentVersion 当前插件版本号
     * @param constraint     约束条件（如 ">=1.2.0-beta"）
     * @return 是否满足条件
     */
    public static boolean checkVersionConstraint(String currentVersion, String constraint) {
        Version currentVer = parseVersion(currentVersion);

        // 解析约束条件中的操作符和版本
        String[] parsed = parseConstraint(constraint);
        if (parsed == null) return false;
        String operator = parsed[0];
        Version targetVersion = parseVersion(parsed[1]);

        // 执行版本比较
        int result = compareVersions(currentVer, targetVersion);

        // 根据操作符判断结果
        switch (operator) {
            case ">=":
                return result >= 0;
            case "<=":
                return result <= 0;
            case ">":
                return result > 0;
            case "<":
                return result < 0;
            case "==":
                return result == 0;
            case "!=":
                return result != 0;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    /**
     * 解析约束条件（操作符 + 版本号）
     */
    private static String[] parseConstraint(String constraint) {
        Pattern pattern = Pattern.compile("^(>=|<=|>|<|==|!=)?\\s*([0-9.]+(?:-[a-zA-Z0-9.-]*)?)$");
        Matcher matcher = pattern.matcher(constraint.trim());
        if (!matcher.find()) return null;

        String operator = matcher.group(1) != null ? matcher.group(1) : "==";
        String version = matcher.group(2);
        return new String[]{operator, version};
    }

    /**
     * 解析版本字符串为 Version 对象
     */
    public static Version parseVersion(String versionStr) {
        String[] parts = versionStr.split("-", 2);
        String versionPart = parts[0];
        String suffix = parts.length > 1 ? parts[1] : "";

        String[] numbers = versionPart.split("\\.");
        int major = numbers.length > 0 ? Integer.parseInt(numbers[0]) : 0;
        int minor = numbers.length > 1 ? Integer.parseInt(numbers[1]) : 0;
        int patch = numbers.length > 2 ? Integer.parseInt(numbers[2]) : 0;

        return new Version(major, minor, patch, suffix);
    }

    /**
     * 核心比较逻辑
     */
    private static int compareVersions(Version v1, Version v2) {
        // 比较主版本号
        int cmp = Integer.compare(v1.getMajor(), v2.getMajor());
        if (cmp != 0) return cmp;

        // 比较次版本号
        cmp = Integer.compare(v1.getMinor(), v2.getMinor());
        if (cmp != 0) return cmp;

        // 比较修订号
        cmp = Integer.compare(v1.getPatch(), v2.getPatch());
        if (cmp != 0) return cmp;

        // 处理后缀逻辑
        boolean hasSuffix1 = !v1.getSuffix().isEmpty();
        boolean hasSuffix2 = !v2.getSuffix().isEmpty();

        // 情况1: 两个版本都有后缀，按字典序比较
        if (hasSuffix1 && hasSuffix2) {
            return v1.getSuffix().compareTo(v2.getSuffix());
        }
        // 情况2: 仅一个版本有后缀，无后缀的版本更高
        else if (hasSuffix1) {
            return -1; // v1有后缀，v2无后缀 → v1 < v2
        } else if (hasSuffix2) {
            return 1;  // v2有后缀，v1无后缀 → v1 > v2
        }

        return 0; // 两者均无后缀且主版本相同
    }

}
