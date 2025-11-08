package cn.memoryzy.json.constant;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.util.SystemInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Memory
 * @since 2025/10/16
 */
public class PathManager {

    private static final String DATA_DIRECTORY_NAME = "JsonAssistantPlugin";
    private static final String FONTS_DIRECTORY_NAME = "Fonts";
    private static final String LIB_DIRECTORY_NAME = "lib";
    private static final String TRANSLATION_DIRECTORY_NAME = "TranslationPlugin";

    private static final String USER_HOME_PATH = System.getProperty("user.home");

    /**
     * 基础目录
     *
     * <p>- Windows: "%LOCALAPPDATA%\MemoryZy\"</p>
     * <p>- Other OS: "$XDG_DATA_HOME/MemoryZy/" or "~/.MemoryZy/"</p>
     */
    public static final Path BASE_DIRECTORY = getBaseDirectory();

    /**
     * The data root directory of this plugin. Usually at the following locations:
     *
     *
     * <p>- Windows: "%LOCALAPPDATA%\MemoryZy\JsonAssistantPlugin\"</p>
     * <p>- Other OS: "$XDG_DATA_HOME/MemoryZy/JsonAssistantPlugin/" or "~/.MemoryZy/JsonAssistantPlugin/"</p>
     */
    public static final Path DATA_DIRECTORY = getDataDirectory();

    /**
     * The data root directory of this plugin. Usually at the following locations:
     *
     *
     * <p>- Windows: "%LOCALAPPDATA%\MemoryZy\Fonts\"</p>
     * <p>- Other OS: "$XDG_DATA_HOME/MemoryZy/Fonts/" or "~/.MemoryZy/Fonts/"</p>
     */
    public static final Path FONTS_DIRECTORY = getFontsDirectory();

    /**
     * 依赖包目录
     */
    public static final Path LIB_DIRECTORY = getLibDirectory();

    /**
     * Translation 插件的专属目录
     */
    public static final Path TRANSLATION_DIRECTORY = getTranslationDirectory();


    /**
     * Creates the data directory if it not exists,
     * and create all nonexistent parent directories first.
     */
    public static void createDataDirectoriesIfNotExists() throws IOException {
        if (!Files.exists(DATA_DIRECTORY) || !Files.isDirectory(DATA_DIRECTORY)) {
            Files.createDirectories(DATA_DIRECTORY);
        }
    }

    /**
     * Creates the fonts directory if it not exists,
     * and create all nonexistent parent directories first.
     */
    public static void createFontsDirectoriesIfNotExists() throws IOException {
        if (!Files.exists(FONTS_DIRECTORY) || !Files.isDirectory(FONTS_DIRECTORY)) {
            Files.createDirectories(FONTS_DIRECTORY);
        }
    }

    public static void createLibDirectoriesIfNotExists() throws IOException {
        if (!Files.exists(LIB_DIRECTORY) || !Files.isDirectory(LIB_DIRECTORY)) {
            Files.createDirectories(LIB_DIRECTORY);
        }
    }


    private static Path getBaseDirectory() {
        String envVarValue = System.getenv(SystemInfo.isWindows ? "LOCALAPPDATA" : "XDG_DATA_HOME");
        return StrUtil.isNotBlank(envVarValue)
                ? Paths.get(envVarValue, "MemoryZy")
                : Paths.get(USER_HOME_PATH, ".MemoryZy");
    }

    private static Path getDataDirectory() {
        Path baseDirectory = getBaseDirectory();
        return baseDirectory.resolve(DATA_DIRECTORY_NAME);
    }

    private static Path getFontsDirectory() {
        Path baseDirectory = getBaseDirectory();
        return baseDirectory.resolve(FONTS_DIRECTORY_NAME);
    }

    private static Path getLibDirectory() {
        Path baseDirectory = getBaseDirectory();
        return baseDirectory.resolve(LIB_DIRECTORY_NAME);
    }

    private static Path getTranslationDirectory() {
        String envVarValue = System.getenv(SystemInfo.isWindows ? "LOCALAPPDATA" : "XDG_DATA_HOME");
        return StrUtil.isNotBlank(envVarValue)
                ? Paths.get(envVarValue, "Yii.Guxing", TRANSLATION_DIRECTORY_NAME)
                : Paths.get(USER_HOME_PATH, "." + TRANSLATION_DIRECTORY_NAME);
    }
}
