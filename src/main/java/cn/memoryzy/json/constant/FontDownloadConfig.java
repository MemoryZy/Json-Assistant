package cn.memoryzy.json.constant;

import cn.memoryzy.json.util.PlatformUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 字体下载配置
 */
public class FontDownloadConfig {
    public static final int MIN_FONT_SIZE = 8_000_000;  // 8MB
    public static final int MAX_RETRIES_PER_URL = 3;

    /**
     * 获取字体下载URL的优先级列表
     *
     * @return URL优先级列表
     */
    public static List<String> getFontUrlPriority() {
        List<String> urls = new ArrayList<>();

        // 首选 Cloudflare URL
        urls.add(Urls.CF_FONT_URL);

        // 如果是中文环境，优先使用 Gitee
        if (PlatformUtil.isChineseLocale()) {
            urls.add(Urls.GITEE_FONT_URL);
        } else {
            urls.add(Urls.GITHUB_FONT_URL);
        }

        return urls;
    }
}