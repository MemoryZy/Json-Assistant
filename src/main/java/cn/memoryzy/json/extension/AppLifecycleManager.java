package cn.memoryzy.json.extension;

import cn.memoryzy.json.service.ConfigurationMerger;
import cn.memoryzy.json.util.UIUtils;
import com.intellij.ide.AppLifecycleListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Memory
 * @since 2025/6/4
 */
public class AppLifecycleManager implements AppLifecycleListener {

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        // 合并旧配置
        ConfigurationMerger.getInstance().mergeGlobalLegacySettings();
        // 初始化字体文件
        UIUtils.loadAndDownloadJetbrainsMapleMonoFont();
    }

}
