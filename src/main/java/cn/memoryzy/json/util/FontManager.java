package cn.memoryzy.json.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.constant.PathManager;
import cn.memoryzy.json.constant.Urls;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Memory
 * @since 2026/4/28
 */
public class FontManager {

    private static final Logger LOG = Logger.getInstance(FontManager.class);

    /**
     * JetBrains Maple Mono 融合字体（支持中文）
     */
    private static Font JETBRAINS_MAPLE_MONO_FONT = null;

    /**
     * JetBrains Maple Mono 融合字体压缩包名
     */
    public static final String JETBRAINS_MAPLE_MONO_ZIP_NAME = "JetBrainsMapleMono-XX-NR-XX.zip";

    /**
     * JetBrains Maple Mono 融合字体名称
     */
    public static final String JETBRAINS_MAPLE_MONO_FONT_NAME = "JetBrainsMapleMono-Light.ttf";

    /**
     * 字体压缩包路径
     */
    private static final String JETBRAINS_MAPLE_MONO_FONT_ZIP_FILE_PATH = PathManager.BASE_DIRECTORY + File.separator + JETBRAINS_MAPLE_MONO_ZIP_NAME;

    /**
     * JetBrains Maple Mono 融合字体路径
     */
    private static final String JETBRAINS_MAPLE_MONO_FONT_FILE_PATH = PathManager.FONTS_DIRECTORY + File.separator + JETBRAINS_MAPLE_MONO_FONT_NAME;

    /**
     * 字体压缩包的 SHA-256
     */
    private static final String EXPECTED_FONT_ZIP_HASH = "8139235ee73b71b156f764b0e23ffeb02e3fdb5cb7701216ac34284661d11e1b";

    /**
     * 字体的 SHA-256
     */
    private static final String EXPECTED_FONT_HASH = "8fc48787877be1f576c31feb50f5b20f7b0050652e5a17b459047cb21682d727";


    /**
     * 加载 JetBrains Maple Mono 字体
     */
    public static void loadAndDownloadJetbrainsMapleMonoFont() {
        // 建立一系列的父目录
        PathManager.createFontsDirectoriesIfNotExists();

        // 检测字体是否存在
        File file = new File(JETBRAINS_MAPLE_MONO_FONT_FILE_PATH);
        if (FileUtil.exist(file) && EXPECTED_FONT_HASH.equals(DigestUtil.sha256Hex(file))) {
            loadJetbrainsMapleMonoFont();

        } else {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                boolean success = false;

                // 按照优先级尝试不同URL
                for (String fontUrl : FontDownloadConfig.getFontUrlPriority()) {
                    LOG.info("## 开始尝试从URL下载字体: " + JsonAssistantUtil.maskUrl(fontUrl));

                    if (downloadAndProcessFontWithRetry(fontUrl)) {
                        success = true;
                        break;
                    }
                }

                if (success) {
                    loadJetbrainsMapleMonoFont();
                    LOG.info("## 字体加载成功");
                } else {
                    LOG.warn("## 字体加载失败，所有URL尝试均失败");
                }
            });
        }
    }

    /**
     * 下载并处理字体文件（带重试机制）
     *
     * @param fontUrl 字体URL
     * @return 是否成功
     */
    private static boolean downloadAndProcessFontWithRetry(String fontUrl) {
        File zipFile = new File(JETBRAINS_MAPLE_MONO_FONT_ZIP_FILE_PATH);

        for (int retryCount = 1; retryCount <= FontDownloadConfig.MAX_RETRIES_PER_URL; retryCount++) {
            try {
                LOG.info(StrUtil.format("## 字体下载尝试 (URL: {}, 尝试次数: {})", JsonAssistantUtil.maskUrl(fontUrl), retryCount));

                // 下载字体文件
                if (!downloadFontZip(zipFile, fontUrl)) {
                    LOG.warn(StrUtil.format("## 字体下载失败，尝试次数: {}，URL: {}", retryCount, JsonAssistantUtil.maskUrl(fontUrl)));
                    continue;
                }

                // 验证 SHA-256
                String actualHash = DigestUtil.sha256Hex(zipFile);
                if (!Objects.equals(EXPECTED_FONT_ZIP_HASH, actualHash)) {
                    LOG.warn(StrUtil.format("## SHA-256验证失败，期望: {}，实际: {}，尝试次数: {}，URL: {}",
                            EXPECTED_FONT_ZIP_HASH.substring(0, 16),
                            actualHash.substring(0, 16),
                            retryCount,
                            JsonAssistantUtil.maskUrl(fontUrl)));
                    FileUtil.del(zipFile);
                    continue;
                }

                // 解压并验证字体文件
                if (extractAndVerifyFont(zipFile)) {
                    LOG.info(StrUtil.format("## 字体下载验证成功，URL: {}", JsonAssistantUtil.maskUrl(fontUrl)));
                    return true;
                }

            } catch (Exception e) {
                LOG.warn(StrUtil.format("## 字体处理过程出错，URL: {}，尝试次数: {}，错误: {}", JsonAssistantUtil.maskUrl(fontUrl), retryCount, e.getMessage()));
                if (zipFile.exists()) {
                    FileUtil.del(zipFile);
                }
            }

            // 重试前等待一段时间（指数退避）
            if (retryCount < FontDownloadConfig.MAX_RETRIES_PER_URL) {
                waitBeforeRetry(retryCount);
            }
        }

        // 清理临时文件
        if (zipFile.exists()) {
            FileUtil.del(zipFile);
        }

        return false;
    }


    /**
     * 下载字体ZIP文件
     *
     * @param outputFile 输出文件
     * @param fontUrl    字体URL
     * @return 是否下载成功
     */
    private static boolean downloadFontZip(File outputFile, String fontUrl) {
        try {
            // 确保父目录存在
            FileUtil.mkParentDirs(outputFile);

            // 清理旧文件
            if (FileUtil.exist(outputFile)) {
                FileUtil.del(outputFile);
            }

            // 下载文件
            long size = HttpUtil.downloadFile(fontUrl, outputFile);

            // 验证文件大小
            if (size < FontDownloadConfig.MIN_FONT_SIZE) {
                LOG.warn(StrUtil.format("## 字体文件大小不足: {} bytes，最小要求: {} bytes", size, FontDownloadConfig.MIN_FONT_SIZE));
                FileUtil.del(outputFile);
                return false;
            }

            LOG.info(StrUtil.format("## 字体下载完成，大小: {} bytes，URL: {}", size, JsonAssistantUtil.maskUrl(fontUrl)));
            return true;

        } catch (Exception e) {
            LOG.warn(StrUtil.format("## 字体下载异常，URL: {}，错误: {}", JsonAssistantUtil.maskUrl(fontUrl), e.getMessage()));
            if (FileUtil.exist(outputFile)) {
                FileUtil.del(outputFile);
            }
            return false;
        }
    }


    /**
     * 解压并验证字体文件
     *
     * @param zipFile ZIP文件
     * @return 是否成功
     */
    private static boolean extractAndVerifyFont(File zipFile) {
        try {
            if (!extractFontFromZip(zipFile)) {
                LOG.warn("## 字体解压失败");
                return false;
            }

            // 验证解压后的字体文件
            File fontFile = new File(JETBRAINS_MAPLE_MONO_FONT_FILE_PATH);
            if (!FileUtil.exist(fontFile)) {
                LOG.warn(StrUtil.format("## 字体文件不存在: {}", JETBRAINS_MAPLE_MONO_FONT_FILE_PATH));
                return false;
            }

            // 验证字体文件哈希
            String fontHash = DigestUtil.sha256Hex(fontFile);
            if (!Objects.equals(EXPECTED_FONT_HASH, fontHash)) {
                LOG.warn(StrUtil.format("## 解压后字体哈希不匹配，期望: {}，实际: {}",
                        EXPECTED_FONT_HASH.substring(0, 16),
                        fontHash.substring(0, 16)));
                FileUtil.del(fontFile);
                return false;
            }

            return true;

        } catch (Exception e) {
            LOG.warn(StrUtil.format("## 字体解压验证异常: {}", e.getMessage()));
            return false;
        }
    }

    /**
     * 重试前等待（指数退避策略）
     *
     * @param retryCount 当前重试次数
     */
    private static void waitBeforeRetry(int retryCount) {
        try {
            // 指数退避：2^retryCount 秒
            long waitTime = (long) Math.pow(2, retryCount) * 1000;
            Thread.sleep(Math.min(waitTime, 10000)); // 最多等待10秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean extractFontFromZip(File zipFile) {
        // 解压到指定目录下，不保留原压缩目录名
        try {
            // 先清空指定目录
            File file = PathManager.FONTS_DIRECTORY.toFile();
            FileUtil.clean(file);
            // 解压
            ZipUtil.unzip(zipFile, file);
            // 移动压缩包
            FileUtil.move(zipFile, new File(PathManager.FONTS_DIRECTORY + File.separator + JETBRAINS_MAPLE_MONO_ZIP_NAME), true);
        } catch (Exception e) {
            LOG.warn("## 解压失败: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    private static synchronized void loadJetbrainsMapleMonoFont() {
        // 存在则加载出来
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(JETBRAINS_MAPLE_MONO_FONT_FILE_PATH));
            // 设置默认大小
            font = font.deriveFont(13f);

            // 给 JETBRAINS_MAPLE_MONO_FONT 变量赋值
            JETBRAINS_MAPLE_MONO_FONT = font;

        } catch (Exception e) {
            LOG.warn("## 字体加载失败: " + e.getMessage(), e);
        }
    }

    public static JBFont consolasFont(int size) {
        return JBUI.Fonts.create("Consolas", size);
    }

    public static JBFont consolasFont(int size, int style) {
        JBFont font = consolasFont(size);

        switch (style) {
            case Font.BOLD:
                font = font.asBold();
                break;
            case Font.ITALIC:
                font = font.asItalic();
                break;
        }

        return font;
    }

    public static JBFont jetBrainsMonoFont(int size) {
        return JBUI.Fonts.create("JetBrains Mono", size);
    }

    public static JBFont microsoftYaHeiUIFont(int size) {
        return JBUI.Fonts.create("Microsoft YaHei UI", size);
    }

    public static JBFont microsoftYaHeiUIFont(int size, int style) {
        JBFont font = JBUI.Fonts.create("Microsoft YaHei UI", size);
        switch (style) {
            case Font.BOLD:
                font = font.asBold();
                break;
            case Font.ITALIC:
                font = font.asItalic();
                break;
        }

        return font;
    }

    /**
     * 获取支持中文的字体（在旧 UI 中的 List和 Tree 的高亮中，如果使用 JetBrainsMono 字体，会乱码）
     *
     * @return 字体
     */
    public static Font getChineseFont(float size) {
        return UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(size);
    }



    public static Font getJetbrainsMapleMonoFont() {
        return JETBRAINS_MAPLE_MONO_FONT;
    }

    /**
     * 字体下载配置
     */
    private static class FontDownloadConfig {
        public static final int MIN_FONT_SIZE = 8_000_000;  // 8MB
        public static final int MAX_RETRIES_PER_URL = 3;

        /**
         * 获取字体下载URL的优先级列表
         *
         * @return URL优先级列表
         */
        public static java.util.List<String> getFontUrlPriority() {
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

}
