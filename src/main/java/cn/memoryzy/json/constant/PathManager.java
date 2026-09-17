package cn.memoryzy.json.constant;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.project.ProjectKt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Memory
 * @since 2025/10/16
 */
public class PathManager {

    private static final String PLUGIN_DIRECTORY_NAME = "JsonAssistantPlugin";
    private static final String FONTS_DIRECTORY_NAME = "Fonts";

    private static final String PROJECT_PLUGIN_DIRECTORY_NAME = "JsonAssistant";
    private static final String PROJECT_HISTORIES_DIRECTORY_NAME = ".histories";

    public static final String PROJECTS_DIRECTORY_NAME = "Projects";

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
    public static final Path PLUGIN_DIRECTORY = getPluginDirectory();

    /**
     * The data root directory of this plugin. Usually at the following locations:
     *
     *
     * <p>- Windows: "%LOCALAPPDATA%\MemoryZy\Fonts\"</p>
     * <p>- Other OS: "$XDG_DATA_HOME/MemoryZy/Fonts/" or "~/.MemoryZy/Fonts/"</p>
     */
    public static final Path FONTS_DIRECTORY = getFontsDirectory();


    /**
     * Creates the data directory if it not exists,
     * and create all nonexistent parent directories first.
     */
    public static void createPluginDirectoriesIfNotExists() {
        if (!Files.exists(PLUGIN_DIRECTORY) || !Files.isDirectory(PLUGIN_DIRECTORY)) {
            FileUtil.mkdir(PLUGIN_DIRECTORY);
        }
    }

    /**
     * Creates the fonts directory if it not exists,
     * and create all nonexistent parent directories first.
     */
    public static void createFontsDirectoriesIfNotExists() {
        if (!Files.exists(FONTS_DIRECTORY) || !Files.isDirectory(FONTS_DIRECTORY)) {
            FileUtil.mkdir(FONTS_DIRECTORY);
        }
    }


    private static Path getBaseDirectory() {
        String envVarValue = System.getenv(SystemInfo.isWindows ? "LOCALAPPDATA" : "XDG_DATA_HOME");
        String userHomePath = System.getProperty("user.home");
        return StrUtil.isNotBlank(envVarValue)
                ? Paths.get(envVarValue, "MemoryZy")
                : Paths.get(userHomePath, ".MemoryZy");
    }

    private static Path getPluginDirectory() {
        return BASE_DIRECTORY.resolve(PLUGIN_DIRECTORY_NAME);
    }

    private static Path getFontsDirectory() {
        return BASE_DIRECTORY.resolve(FONTS_DIRECTORY_NAME);
    }


    /**
     * 创建并获取处于项目下的插件目录，若项目类型为基于文件(.ipr)，则使用自定义目录
     * <br></br>
     * <p>- DIRECTORY_BASED: "%PROJECT_DIR%\.idea\JsonAssistant\"</p>
     * <p>- FILE_BASE: "%LOCALAPPDATA%\MemoryZy\JsonAssistantPlugin\Projects\%PROJECT_DIR%"</p>
     *
     * @param project 项目
     * @return 路径
     */
    public static Path createAndGetProjectPluginDirectory(Project project) {
        return createAndGetProjectPluginDirectory(ProjectKt.getStateStore(project), project);
    }

    /**
     * 创建并获取处于项目下的插件目录，若项目类型为基于文件(.ipr)，则使用自定义目录
     * <br></br>
     * <p>- DIRECTORY_BASED: "%PROJECT_DIR%\.idea\JsonAssistant\"</p>
     * <p>- FILE_BASE: "%LOCALAPPDATA%\MemoryZy\JsonAssistantPlugin\Projects\%PROJECT_DIR%\"</p>
     *
     * @param store 项目存储项
     * @return 路径
     */
    public static Path createAndGetProjectPluginDirectory(IProjectStore store, Project project) {
        Path resultPath = null;
        // 获取项目存储类型
        StorageScheme storageScheme = store.getStorageScheme();
        if (StorageScheme.DIRECTORY_BASED == storageScheme) {
            // 基于目录
            Path directoryStorePath = store.getDirectoryStorePath();
            if (null != directoryStorePath) {
                resultPath = directoryStorePath.resolve(PROJECT_PLUGIN_DIRECTORY_NAME);
            }
        }

        if (null == resultPath) {
            // 基于文件(.ipr)
            resultPath = Paths.get(PLUGIN_DIRECTORY.toString(), PROJECTS_DIRECTORY_NAME, project.getName());
        }

        // 创建目录
        if (!Files.exists(resultPath) || !Files.isDirectory(resultPath)) {
            FileUtil.mkdir(resultPath);
        }
        
        return resultPath;
    }


    public static Path createAndGetProjectHistoriesDirectory(Path projectPluginDirectory) {
        Path path = projectPluginDirectory.resolve(PROJECT_HISTORIES_DIRECTORY_NAME);
        // 创建目录
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            FileUtil.mkdir(path);
        }
        return path;
    }

}
