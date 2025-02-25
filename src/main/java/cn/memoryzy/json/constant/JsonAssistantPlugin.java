package cn.memoryzy.json.constant;

import cn.memoryzy.json.model.Version;
import com.intellij.diagnostic.PluginException;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

/**
 * @author Memory
 * @since 2024/7/25
 */
public class JsonAssistantPlugin {

    public static final String PLUGIN_ID = "cn.memoryzy.json";
    public static final String PLUGIN_NAME = "Json Assistant";
    public static final String PLUGIN_ID_NAME = PLUGIN_ID + ".Json-Assistant";
    public static final String PLUGIN_VERSION = PLUGIN_ID + ".version";

    private static final IdeaPluginDescriptor descriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));

    public static IdeaPluginDescriptor getJsonAssistant() {
        if (descriptor == null) {
            throw new PluginException("Plugin does not exist!", PluginId.getId(PLUGIN_ID));
        }

        return descriptor;
    }

    public static String getVersion() {
        return getJsonAssistant().getVersion();
    }

    public static boolean isNewerVersion(String existingVersion, String newVersion) {
        // 解析版本号为 Major, Minor, Patch 和 Suffix
        Version parsedExisting = parseVersion(existingVersion);
        Version parsedNew = parseVersion(newVersion);

        // 比较主版本号、次版本号和补丁版本号
        int comparisonResult = compareVersionComponents(parsedExisting, parsedNew);
        if (comparisonResult != 0) {
            return comparisonResult > 0;
        }

        // 如果主版本号、次版本号和补丁版本号相同，则比较后缀
        return compareSuffix(parsedExisting.getSuffix(), parsedNew.getSuffix()) > 0;
    }

    private static Version parseVersion(String version) {
        String[] parts = version.split("-", 2);
        String versionPart = parts[0];
        String suffix = parts.length > 1 ? parts[1] : "";

        String[] versionNumbers = versionPart.split("\\.");
        int major = versionNumbers.length > 0 ? Integer.parseInt(versionNumbers[0]) : 0;
        int minor = versionNumbers.length > 1 ? Integer.parseInt(versionNumbers[1]) : 0;
        int patch = versionNumbers.length > 2 ? Integer.parseInt(versionNumbers[2]) : 0;

        return new Version(major, minor, patch, suffix);
    }

    private static int compareVersionComponents(Version v1, Version v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return Integer.compare(v2.getMajor(), v1.getMajor()); // 注意这里是 v2.major 和 v1.major 的顺序
        }
        if (v1.getMinor() != v2.getMinor()) {
            return Integer.compare(v2.getMinor(), v1.getMinor());
        }
        return Integer.compare(v2.getPatch(), v1.getPatch());
    }

    private static int compareSuffix(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) {
            return 0;
        }
        if (s1.isEmpty()) {
            return -1; // 没有后缀的版本被认为是较新的
        }
        if (s2.isEmpty()) {
            return 1;
        }

        // 尝试按字典顺序比较后缀
        return s1.compareTo(s2);
    }
}
